package com.apoorvdarshan.calorietracker.kindee

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.CancellationSignal
import androidx.compose.animation.animateContentSize
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.health.connect.client.PermissionController
import com.apoorvdarshan.calorietracker.AppContainer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZoneId
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun KindeePlanScreen(container: AppContainer, openSettings: () -> Unit, openWorkouts: () -> Unit) {
    val context = LocalContext.current
    val client = remember { KindeeClient(context,container.keyStore) }
    val scope = rememberCoroutineScope()
    var connected by remember { mutableStateOf(client.connected) }
    var busy by remember { mutableStateOf(false) }
    var problem by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var profile by remember { mutableStateOf<JSONObject?>(null) }
    var place by remember { mutableStateOf<FoodPlace?>(null) }
    var menuNote by remember { mutableStateOf("Saved menus · Confirm availability") }
    var offers by remember { mutableStateOf<List<JSONObject>>(emptyList()) }
    var previousPlacePlan by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf<List<JSONObject>>(emptyList()) }
    var draft by remember { mutableStateOf<PlanDraft?>(null) }
    var includeUnknown by remember { mutableStateOf(false) }
    var selectedDay by remember { mutableStateOf<String?>(null) }
    var nearby by remember { mutableStateOf(false) }
    var accountSheet by remember { mutableStateOf(false) }
    var addressSheet by remember { mutableStateOf(false) }
    var address by remember { mutableStateOf("") }
    var matches by remember { mutableStateOf<List<FoodPlace>>(emptyList()) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var server by remember { mutableStateOf(client.base) }
    var foodSheet by remember { mutableStateOf(false) }
    var confirmTargets by remember { mutableStateOf(false) }
    var confirmSave by remember { mutableStateOf(false) }
    var planOptions by remember { mutableStateOf(false) }
    var shareDiary by remember { mutableStateOf(false) }
    var shareHealth by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    fun perform(action: suspend () -> Unit) {
        if (busy) return
        scope.launch {
            busy = true; problem = null; message = null
            try { action() } catch (e: CancellationException) { throw e }
            catch (e: Exception) { problem = e.message ?: "Could not complete this request. Try again." }
            finally {
                connected = client.connected; busy = false
                if (!connected) { profile = null; saved = emptyList(); offers = emptyList(); draft = null; place = null }
            }
        }
    }
    suspend fun load() {
        val next = client.profile()
        val food = next.optJSONObject("food_preferences")
        val loc = food?.optJSONObject("location")
        profile = next
        includeUnknown = food?.optBoolean("allow_unverified_menu",false) ?: false
        if (loc != null && !loc.isNull("latitude") && !loc.isNull("longitude")) {
            place = FoodPlace(loc.getString("city"),loc.getDouble("latitude"),loc.getDouble("longitude"),loc.optString("source","manual"))
        }
        previousPlacePlan = false
        saved = client.savedPlan()
        offers = place?.let { client.nearby(it) } ?: emptyList()
        menuNote = client.menuStatus
    }
    fun choose(next: FoodPlace) {
        if (place?.let { kotlin.math.abs(it.latitude-next.latitude)>0.00001 || kotlin.math.abs(it.longitude-next.longitude)>0.00001 } == true) {
            previousPlacePlan = saved.isNotEmpty() || previousPlacePlan
            saved = emptyList()
        }
        place = next; draft = null; matches = emptyList(); addressSheet = false
        perform { offers = client.nearby(next); menuNote = client.menuStatus }
    }
    val healthPermissions = rememberLauncherForActivityResult(PermissionController.createRequestPermissionResultContract()) { granted ->
        if (granted.isEmpty()) problem = "Health permission was not granted. You can keep using the food diary."
        else message = "Activity permissions updated. Sync your watch, then share today's activity."
    }
    val locationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) problem = "Location access was declined. Search by address instead."
        else if (Build.VERSION.SDK_INT >= 30) {
            val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val cancel = CancellationSignal()
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({ cancel.cancel() }, 20000)
            try {
                manager.getCurrentLocation(LocationManager.NETWORK_PROVIDER,cancel,ContextCompat.getMainExecutor(context)) { l ->
                    if (l != null && System.currentTimeMillis() - l.time < 300000) choose(FoodPlace("Current location",l.latitude,l.longitude,"device"))
                    else problem = "No recent position. Search by address instead."
                }
            } catch (_: Exception) { problem = "Location is unavailable. Search by address instead." }
        } else problem = "Use address search on this Android version."
    }
    LaunchedEffect(Unit) { if (connected) perform { load() } }
    LaunchedEffect(message) { message?.let { snackbar.showSnackbar(it) } }
    Scaffold(
        topBar = { TopAppBar(title={Text("Kindee")},actions={IconButton(onClick={accountSheet=true},enabled=!busy){Icon(Icons.Default.AccountCircle,"Kindee account")}}) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { inset ->
        LazyColumn(Modifier.fillMaxSize().padding(inset),contentPadding=PaddingValues(start=20.dp,end=20.dp,bottom=140.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
            item {
                Text("Your meal plan",style=MaterialTheme.typography.headlineLarge)
            }
            if (busy) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            problem?.let { text -> item { Text(text,color=MaterialTheme.colorScheme.error,style=MaterialTheme.typography.bodyLarge) } }
            if (!connected) {
                item {
                    Text("Cook, eat out, order in",style=MaterialTheme.typography.titleLarge)
                    Text("Sign in for nearby menus and your weekly plan.")
                    Button(onClick={accountSheet=true}) { Text("Connect Kindee") }
                }
            } else {
                item {
                    OutlinedButton(onClick={addressSheet=true},enabled=!busy,modifier=Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.LocationOn,null); Spacer(Modifier.width(8.dp))
                        Text(place?.label?.substringBefore(",") ?: "Choose an address",modifier=Modifier.weight(1f),maxLines=1,overflow=TextOverflow.Ellipsis)
                    }
                    Text("5 km · $menuNote",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
                item {
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        listOf("My plan","Nearby menus").forEachIndexed { index,label ->
                            SegmentedButton(selected=nearby==(index==1),onClick={nearby=index==1},shape=SegmentedButtonDefaults.itemShape(index,2)) { Text(label) }
                        }
                    }
                }
                if (!nearby) {
                    val rows = draft?.entries ?: saved
                    val days = rows.map { it.optString("planned_for") }.filter { it.isNotBlank() }.distinct().sorted()
                    val activeDay = selectedDay?.takeIf { it in days } ?: days.firstOrNull { it == LocalDate.now().toString() } ?: days.firstOrNull()
                    if (days.isNotEmpty()) item {
                        LazyRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                            items(days) { day ->
                                val date = runCatching { LocalDate.parse(day) }.getOrNull()
                                FilterChip(selected=day==activeDay,onClick={selectedDay=day},label={
                                    Text(if(date==LocalDate.now()) "Today" else date?.format(DateTimeFormatter.ofPattern("EEE d",Locale.getDefault())) ?: day)
                                })
                            }
                        }
                        if (draft != null) Text("Preview · not saved",style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    item {
                        Column(Modifier.animateContentSize()) {
                            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                                TextButton(enabled=!busy,onClick={foodSheet=true}) { Text("Preferences") }
                                TextButton(enabled=!busy,onClick={planOptions=!planOptions}) { Text(if(planOptions)"Close options" else "Plan options") }
                            }
                            if(planOptions) {
                        Row(verticalAlignment=Alignment.CenterVertically) {
                            Checkbox(checked=includeUnknown,onCheckedChange=if(busy) null else { value -> includeUnknown=value; draft=null })
                            Text("Use AI menu estimates",modifier=Modifier.weight(1f))
                        }
                        Text("Menu names and selected options are sent to OpenAI. Estimates are saved for seven days; review portions.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                            Button(enabled=!busy && place!=null,onClick={
                                perform {
                                    val current = profile ?: error("Reload your account first")
                                    client.updateLocation(current,place!!,includeUnknown)
                                    val food = current.optJSONObject("food_preferences")
                                    val restricted = food?.optString("diet","any") != "any" || (food?.optJSONArray("excluded")?.length() ?: 0) > 0
                                    if (includeUnknown && !restricted) client.estimateMenus()
                                    draft = client.preview(LocalDate.now().toString())
                                    offers = place?.let { client.nearby(it) } ?: emptyList()
                                }
                            }) { Text(if(saved.isEmpty()) "Build my week" else "Rebuild week") }
                            
                        }
                    }
                    draft?.let { current -> item {
                        Text("Saving a plan does not log food as eaten.",style=MaterialTheme.typography.bodySmall)
                        Button(enabled=!busy,onClick={confirmSave=true}) { Text("Save this plan") }
                        TextButton(enabled=!busy,onClick={draft=null}) { Text("Discard preview") }
                    } }
                    if (rows.isEmpty() && !busy) item { Text(if(previousPlacePlan)"Your saved plan uses another address. Build a week here, or reload the saved address." else "No saved plan yet. Choose an address and build your week.") }
                    if (rows.isNotEmpty()) item {
                        val dayMeals = rows.filter { it.optString("planned_for")==activeDay }
                        val known = dayMeals.mapNotNull { it.optJSONObject("nutrition")?.optJSONObject("total")?.optDouble("calories")?.takeIf(Double::isFinite) }
                        val target = profile?.optJSONObject("food_preferences")?.optInt("daily_calories",0)?.takeIf{it>0} ?: profile?.optInt("calorie_goal",2000) ?: 2000
                        val goal = when(profile?.optString("goal_kind")) { "lose" -> "Lose weight"; "gain" -> "Gain weight"; "maintain" -> "Maintain"; else -> "Track" }
                        val food = profile?.optJSONObject("food_preferences")
                        if(food?.optString("diet","any") != "any" || (food?.optJSONArray("excluded")?.length() ?: 0)>0) Text("Ingredient restrictions on · unverified restaurant meals stay out of your plan",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$goal · $target kcal goal",style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(if(known.size==dayMeals.size) "≈ ${known.sum().roundToInt()} kcal planned" else "${known.sum().roundToInt()} kcal + ${dayMeals.size-known.size} unestimated",style=MaterialTheme.typography.titleLarge)
                        if(dayMeals.any{it.optJSONObject("nutrition")?.optString("source")=="ai_menu_estimate"}) Text("Includes AI estimates · check portions",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    itemsIndexed(rows.filter { it.optString("planned_for")==activeDay },key={i,row -> "${row.optString("planned_for")}-${row.optString("meal_slot")}-${i}"}) { _,row ->
                        MealOffer(row,true) { openKindeeOffer(context,row.optJSONObject("menu_item") ?: row) }
                    }
                } else {
                    if (offers.isEmpty() && !busy) item { Text("No saved menus here yet. Try another address or build a cooking plan.") }
                    itemsIndexed(offers,key={i,row -> "${row.optString("source_id")}-${i}"}) { _,row -> MealOffer(row,false) { openKindeeOffer(context,row) } }
                }

            }
        }
    }
    if (confirmTargets) AlertDialog(onDismissRequest={confirmTargets=false},title={Text("Use your Kindee targets?")},text={Text("Replace the diary's calorie and macro targets with those in your Kindee account. Automatic target adjustments will stay off.")},confirmButton={TextButton(onClick={confirmTargets=false;perform {
        val g=client.request("dashboard").getJSONObject("data").getJSONObject("nutrition").getJSONObject("goals")
        val local=container.profileRepository.current() ?: error("Complete your diary profile first")
        val kcal=g.getInt("calories");check(kcal in 800..6000){"Review your Kindee targets first"}
        container.profileRepository.save(local.copy(customCalories=kcal,customProtein=g.getInt("protein_g"),customFat=g.getInt("fat_g"),customCarbs=g.getInt("carbs_g"),autoBalanceMacro=null))
        container.prefs.setAdaptiveGoalsEnabled(false);message="Kindee nutrition targets applied";accountSheet=false
    }}){Text("Use targets")}},dismissButton={TextButton(onClick={confirmTargets=false}){Text("Cancel")}})
    if (confirmSave) AlertDialog(onDismissRequest={confirmSave=false},title={Text("Save this week?")},text={Text("This replaces generated meals in the selected seven days. Manually added meals stay. Saving a plan does not log food as eaten.")},confirmButton={TextButton(onClick={confirmSave=false;draft?.let { current -> perform {client.save(current);saved=client.savedPlan();draft=null;message="Your meal plan is saved"} }}) {Text("Save week")}},dismissButton={TextButton(onClick={confirmSave=false}){Text("Cancel")}})
    if (shareDiary) AlertDialog(onDismissRequest={shareDiary=false},title={Text("Share your food diary?")},text={Text("Send today's reviewed meal names, time and nutrition to your Kindee account. Photos are not included. Repeating this updates the same entries; it does not delete older server entries.")},confirmButton={TextButton(onClick={shareDiary=false; perform { val count=shareKindeeDiary(client,container.foodRepository.entriesForDate(LocalDate.now()).first()); message="Shared $count reviewed entries" }}){Text("Share diary")}},dismissButton={TextButton(onClick={shareDiary=false}){Text("Cancel")}})
    if (shareHealth) AlertDialog(onDismissRequest={shareHealth=false},title={Text("Share activity with Kindee?")},text={Text("Send today's available steps, active and total energy from Health Connect. Kindee will use this aggregate as the selected activity source. It will not automatically increase your food target.")},confirmButton={TextButton(onClick={shareHealth=false;perform { shareKindeeActivity(client,container.health);message="Activity shared with Kindee" }}){Text("Share activity")}},dismissButton={TextButton(onClick={shareHealth=false}){Text("Cancel")}})
    if (accountSheet) ModalBottomSheet(sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true),onDismissRequest={if(!busy)accountSheet=false}) {
        Column(Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState()).padding(20.dp).padding(bottom=32.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text("Kindee account",style=MaterialTheme.typography.headlineSmall)
            if (!connected) {
                OutlinedTextField(server,{server=it},label={Text("Kindee server")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Uri,autoCorrectEnabled=false),singleLine=true,modifier=Modifier.fillMaxWidth(),enabled=!busy)
                OutlinedTextField(email,{email=it},label={Text("Email")},singleLine=true,keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Email),modifier=Modifier.fillMaxWidth(),enabled=!busy)
                OutlinedTextField(password,{password=it},label={Text("Password")},singleLine=true,visualTransformation=PasswordVisualTransformation(),modifier=Modifier.fillMaxWidth(),enabled=!busy)
                Button(enabled=!busy && email.isNotBlank() && password.isNotBlank(),onClick={perform { client.login(server,email,password); password=""; connected=true; load(); accountSheet=false }}) { Text(if(busy)"Connecting…" else "Sign in") }
                problem?.let { Text(it,color=MaterialTheme.colorScheme.error) }
            } else {
                Text("Menus and plans sync with Kindee. Your diary stays on this device until you share it.")
                Text("Kindee AI sends submitted food photos and descriptions to OpenAI. In Coach, your message, profile and nutrition summary are also sent. Review AI estimates.")
                Button(enabled=!busy,onClick={perform {client.connectFoodAi(container.prefs);message="Kindee AI connected for food and Coach.";accountSheet=false}}) { Text("Connect Kindee AI") }
                TextButton(enabled=!busy,onClick={accountSheet=false;shareDiary=true}) { Text("Share today's reviewed diary") }
                TextButton(enabled=!busy,onClick={accountSheet=false;healthPermissions.launch(container.health.kindeeActivityPermissions())}) { Text("Connect a watch") }
                TextButton(enabled=!busy,onClick={accountSheet=false;shareHealth=true}) { Text("Share today's activity") }
                TextButton(enabled=!busy,onClick={perform { draft=null; load(); message="Plan refreshed"; accountSheet=false }}) { Text("Refresh plan") }
                TextButton(onClick={accountSheet=false;openWorkouts()}) { Text("Workout diary") }
                TextButton(enabled=!busy,onClick={confirmTargets=true}) { Text("Use Kindee nutrition targets") }
                TextButton(onClick={accountSheet=false;openSettings()}) { Text("AI and health settings") }
                TextButton(enabled=!busy,onClick={perform {client.logout();accountSheet=false}}) { Text("Sign out of Kindee") }
            }
        }
    }
    if (foodSheet) KindeeFoodPreferences(JSONObject(profile?.optJSONObject("food_preferences")?.toString() ?: "{}").apply {
        if (isNull("daily_calories")) put("daily_calories",profile?.optInt("calorie_goal",2000) ?: 2000)
    },profile?.optString("goal_kind","track") ?: "track",busy,{foodSheet=false}) { food,goal ->
        perform {
            client.updateLocation(profile ?: error("Reload your profile"),place ?: error("Choose an address"),includeUnknown,food,goal)
            draft=null;load();foodSheet=false;message="Food preferences saved"
        }
    }
    if (addressSheet) ModalBottomSheet(sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true),onDismissRequest={if(!busy)addressSheet=false}) {
        Column(Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState()).padding(20.dp).padding(bottom=32.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text("Where will you eat?",style=MaterialTheme.typography.headlineSmall)
            OutlinedTextField(address,{address=it;matches=emptyList()},label={Text("Address or place")},modifier=Modifier.fillMaxWidth(),enabled=!busy)
            Text("Search sends this address to OpenStreetMap Nominatim.",style=MaterialTheme.typography.bodySmall)
            Button(enabled=!busy && address.trim().length>=3,onClick={perform { matches=client.search(address); if(matches.isEmpty())problem="No matching address. Try a more specific place." }}) { Text("Search") }
            matches.forEach { match -> TextButton(enabled=!busy,onClick={choose(match)}) { Text(match.label) } }
            TextButton(enabled=!busy,onClick={locationPermission.launch(Manifest.permission.ACCESS_COARSE_LOCATION)}) { Text("Use current location") }
            TextButton(enabled=!busy,onClick={choose(Homa)}) { Text("Homa Phuket Town · test location") }
            problem?.let { Text(it,color=MaterialTheme.colorScheme.error) }
        }
    }
}

@Composable
private fun MealOffer(row: JSONObject, planned: Boolean, prominent: Boolean = false, onOpen: () -> Unit) {
    val menu = row.optJSONObject("menu_item") ?: row
    val context = LocalContext.current
    var details by remember(row.toString()) { mutableStateOf(false) }
    val nutrition = row.optJSONObject("nutrition") ?: menu.optJSONObject("nutrition")
    val ai = nutrition?.optString("source") == "ai_menu_estimate"
    val calories = nutrition?.optJSONObject("total")?.optDouble("calories")?.takeIf(Double::isFinite)
    val restaurant = menu.optString("restaurant")
    val price = menu.optJSONObject("price")
    val priceText = if(price!=null) (if(menu.optBoolean("price_is_base")) "From " else "")+"${java.text.DecimalFormat("0.##").format(price.optDouble("amount_minor")/100)} ${price.optString("currency")}" else ""
    val image = menu.optString("image_url").takeIf { value ->
        val uri=Uri.parse(value)
        uri.scheme=="https" && uri.host=="img.wongnai.com" && uri.userInfo==null && uri.port==-1
    }
    Column(Modifier.fillMaxWidth().animateContentSize(),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        if (planned) Text(row.optString("meal_slot").replaceFirstChar { it.uppercase() } + " · " + when(row.optString("fulfillment")) { "cafe" -> "Cafe"; "delivery" -> "Delivery"; else -> "Cook" },style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.primary)
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            if(image!=null) {
                var unavailable by remember(image) { mutableStateOf(false) }
                Box(Modifier.size(104.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceVariant),contentAlignment=Alignment.Center) {
                    if(unavailable) Text("Photo unavailable",Modifier.padding(8.dp),style=MaterialTheme.typography.bodySmall)
                    else AsyncImage(model=ImageRequest.Builder(context).data(image).setHeader("User-Agent","Kindee/0.5").crossfade(true).build(),
                        contentDescription="Menu photo of ${row.optString("title")}",contentScale=ContentScale.Crop,modifier=Modifier.fillMaxSize(),onError={unavailable=true})
                }
            }
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)) {
                Text(row.optString("title"),style=MaterialTheme.typography.titleLarge)
                if (restaurant.isNotEmpty()) Text(restaurant,style=MaterialTheme.typography.bodyMedium)
                if(priceText.isNotEmpty()) Text(priceText,style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
                Text(if(calories!=null) "≈ ${calories.roundToInt()} kcal · ${if(ai) "AI estimate" else "recipe estimate"}" else "Calories not estimated",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if(row.optString("offer_status")=="search_needed") Text("Recipe idea · no matching menu yet",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        val choices=menu.optJSONArray("selected_options")?.let{array -> (0 until array.length()).map{array.optString(it)}.joinToString(", ")}.orEmpty()
        if(choices.isNotBlank()) Text("Selected: $choices",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        if (restaurant.isNotEmpty()) {
            if (menu.has("restaurant_open") && !menu.isNull("restaurant_open") && !menu.optBoolean("restaurant_open")) Text("Closed at last check",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically) {
                TextButton(onClick=onOpen) { Text(if(menu.optString("provider")=="grab") "Open Grab" else if(menu.optString("provider")=="line_man") "Open LINE MAN" else "View menu") }
                TextButton(onClick={details=!details}) { Text(if(details) "Less" else "Menu details") }
            }
            if (details) {
                Text("${providerLabel(menu.optString("provider"))} · checked ${menuCheckedTime(menu.optString("checked_at"))}",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                if(ai) {
                    val range=nutrition?.optJSONObject("calorie_range")
                    val total=nutrition?.optJSONObject("total")
                    Text("${nutrition?.optString("portion_description")} · ${nutrition?.optInt("portion_grams")} g assumed",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${range?.optInt("min")}–${range?.optInt("max")} kcal · P ${total?.optDouble("protein_g")?.roundToInt()} g · C ${total?.optDouble("carbs_g")?.roundToInt()} g · F ${total?.optDouble("fat_g")?.roundToInt()} g",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Estimated from menu text, not measured. Check ingredients and portion before logging.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("Confirm ingredients, delivery and final price. Menu photos may show another option.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else Text("${row.optInt("servings",1)} serving(s)" + (row.optInt("minutes").takeIf{it>0}?.let{" · $it min"} ?: ""),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
        HorizontalDivider(Modifier.padding(top=8.dp))
    }
}

internal fun openKindeeOffer(context: Context, row: JSONObject) {
    val raw= row.optString("order_url").takeIf{it.isNotBlank() && it!="null"} ?: row.optString("source_url")
    val u=Uri.parse(raw)
    val allowed=setOf("app.grab.com","food.grab.com","lin.ee","www.wongnai.com","vivarooftopandrestaurant.com")
    if(u.scheme!="https" || u.host?.lowercase() !in allowed || u.userInfo!=null || u.port!=-1) return
    val deep=u.getQueryParameter("af_dp")?.let(Uri::parse)
    if(deep?.scheme=="com.linecorp.linemanth" && deep.host=="app" && deep.path?.matches(Regex("/service/food/r/[A-Za-z0-9]+"))==true) {
        try {context.startActivity(Intent(Intent.ACTION_VIEW,deep).setPackage("com.linecorp.linemanth"));return} catch(_:android.content.ActivityNotFoundException){}
    }
    try { context.startActivity(Intent(Intent.ACTION_VIEW,u)) } catch(_:android.content.ActivityNotFoundException){}
}

private fun menuCheckedTime(value: String): String = runCatching {
    val utc = runCatching { OffsetDateTime.parse(value.replace(" ","T")) }.getOrElse {
        java.time.LocalDateTime.parse(value.replace(" ","T")).atOffset(java.time.ZoneOffset.UTC)
    }
    utc.atZoneSameInstant(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("d MMM, HH:mm z",Locale.getDefault()))
}.getOrElse { value.take(16) + " UTC" }

private fun providerLabel(value:String):String = when(value) {
    "merchant_site" -> "Restaurant website"
    "grab" -> "Grab"
    "line_man" -> "LINE MAN"
    else -> "Menu observation"
}
