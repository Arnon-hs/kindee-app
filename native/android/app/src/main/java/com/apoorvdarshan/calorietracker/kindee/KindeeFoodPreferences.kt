package com.apoorvdarshan.calorietracker.kindee

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.json.JSONObject
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class,ExperimentalLayoutApi::class)
@Composable
internal fun KindeeFoodPreferences(initial:JSONObject,initialGoal:String,busy:Boolean,onClose:()->Unit,onSave:(JSONObject,String)->Unit) {
    var goal by remember { mutableStateOf(initialGoal) }
    var calories by remember { mutableStateOf(initial.optInt("daily_calories",2000).toString()) }
    val ingredients=listOf("chicken","shrimp","egg","tofu","rice","broccoli","tomato","spinach","garlic","olive")
    var diet by remember { mutableStateOf(initial.optString("diet","any")) }
    var likes by remember { mutableStateOf(ingredients.filter { initial.optJSONArray("likes")?.toString()?.contains("\"$it\"")==true }.toSet()) }
    var avoided by remember { mutableStateOf(ingredients.filter { initial.optJSONArray("excluded")?.toString()?.contains("\"$it\"")==true }.toSet()) }
    var meals by remember { mutableStateOf(initial.optInt("meals_per_day",3).toString()) }
    var budget by remember { mutableStateOf(initial.optInt("budget_per_meal",350).toString()) }
    var minutes by remember { mutableStateOf(initial.optInt("cooking_minutes",30).toString()) }
    val valid=calories.toIntOrNull() in 800..6000 && meals.toIntOrNull() in 2..4 && budget.toIntOrNull() in 1..2000 && minutes.toIntOrNull() in 10..120
    ModalBottomSheet(onDismissRequest={if(!busy)onClose()},sheetState=rememberModalBottomSheetState(skipPartiallyExpanded=true)) {
        Column(Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState()).padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            Text("Make the plan yours",style=MaterialTheme.typography.headlineMedium)
            FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                listOf("track" to "Track", "maintain" to "Maintain", "lose" to "Lose weight", "gain" to "Gain weight").forEach { (key,label) ->
                    FilterChip(selected=goal==key,onClick={goal=key},label={Text(label)},enabled=!busy)
                }
            }
            OutlinedTextField(calories,{calories=it},label={Text("Daily calorie target")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),singleLine=true,enabled=!busy,modifier=Modifier.fillMaxWidth())
            Text("Your chosen target. Menu calories are estimates.",style=MaterialTheme.typography.bodySmall)
            FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                listOf("any" to "Any food","vegetarian" to "Vegetarian","vegan" to "Vegan","pescatarian" to "Pescatarian").forEach { (key,label) ->
                    FilterChip(selected=diet==key,onClick={diet=key},label={Text(label)},enabled=!busy)
                }
            }
            OutlinedTextField(meals,{meals=it},label={Text("Meals per day · 2 to 4")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),singleLine=true,enabled=!busy,modifier=Modifier.fillMaxWidth())
            OutlinedTextField(budget,{budget=it},label={Text("Budget per meal · THB")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),singleLine=true,enabled=!busy,modifier=Modifier.fillMaxWidth())
            OutlinedTextField(minutes,{minutes=it},label={Text("Cooking time · 10 to 120 min")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),singleLine=true,enabled=!busy,modifier=Modifier.fillMaxWidth())
            Text("Ingredient       Like        Avoid",style=MaterialTheme.typography.labelLarge)
            ingredients.forEach { ingredient ->
                Row(Modifier.fillMaxWidth(),verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) {
                    Text(ingredient.replaceFirstChar{it.uppercase()},Modifier.weight(1f))
                    Checkbox(ingredient in likes,{value -> likes=if(value)likes+ingredient else likes-ingredient;if(value)avoided=avoided-ingredient},enabled=!busy,modifier=Modifier.semanticsLabel("Like $ingredient"))
                    Checkbox(ingredient in avoided,{value -> avoided=if(value)avoided+ingredient else avoided-ingredient;if(value)likes=likes-ingredient},enabled=!busy,modifier=Modifier.semanticsLabel("Avoid $ingredient"))
                }
            }
            if(!valid) Text("Use 800 to 6000 kcal, 2 to 4 meals, 1 to 2000 THB and 10 to 120 minutes.",color=MaterialTheme.colorScheme.error)
            Button(enabled=valid&&!busy,onClick={onSave(JSONObject(initial.toString()).put("diet",diet).put("likes",JSONArray(likes.toList())).put("excluded",JSONArray(avoided.toList())).put("meals_per_day",meals.toInt()).put("budget_per_meal",budget.toInt()).put("cooking_minutes",minutes.toInt()).put("daily_calories",calories.toInt()),goal)},modifier=Modifier.fillMaxWidth()) {Text("Save food preferences")}
            TextButton(onClick=onClose,enabled=!busy) {Text("Cancel")}
            Spacer(Modifier.height(24.dp))
        }
    }
}

private fun Modifier.semanticsLabel(label:String):Modifier = this.then(Modifier.semantics { contentDescription=label })
