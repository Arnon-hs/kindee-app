package com.apoorvdarshan.calorietracker.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.apoorvdarshan.calorietracker.R
import com.apoorvdarshan.calorietracker.models.WorkoutTabMode

data class BottomTab(val route: String, val icon: ImageVector, @get:StringRes val labelRes: Int)

val BottomTabs = listOf(
    BottomTab(FudAIRoutes.PLAN, Icons.Filled.RestaurantMenu, R.string.kindee_plan),
    BottomTab(FudAIRoutes.HOME, Icons.Filled.Home, R.string.nav_home),
    BottomTab(FudAIRoutes.PROGRESS, Icons.Filled.BarChart, R.string.nav_progress),
    BottomTab(FudAIRoutes.COACH, Icons.Filled.Forum, R.string.nav_coach),
    BottomTab(FudAIRoutes.SETTINGS, Icons.Filled.Settings, R.string.nav_settings)
)

/** The Workouts tab icon for the current mode; Settings > Tab Bar shows the same one. */
fun workoutsTabIcon(mode: WorkoutTabMode): ImageVector =
    if (mode == WorkoutTabMode.LOG) Icons.Filled.SportsGymnastics else Icons.Filled.FitnessCenter

val BottomNavScrollPadding = 112.dp
val BottomNavDockedControlPadding = 88.dp

@Composable
fun FudAIBottomNavBar(
    currentRoute: String?,
    showAboutBadge: Boolean = false,
    workoutMode: WorkoutTabMode = WorkoutTabMode.Default,
    hiddenRoutes: Set<String> = emptySet(),
    onTap: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier=modifier,containerColor=MaterialTheme.colorScheme.surface,tonalElevation=0.dp) {
        BottomTabs.filterNot { FudAIRoutes.isHiddenTab(it.route,hiddenRoutes) }.forEach { tab ->
            NavigationBarItem(
                selected=currentRoute==tab.route,
                onClick={onTap(tab.route)},
                icon={Icon(tab.icon,stringResource(tab.labelRes))},
                label={Text(stringResource(tab.labelRes))},
                modifier=Modifier.testTag("nav.${tab.route}"),
                colors=NavigationBarItemDefaults.colors(
                    selectedIconColor=MaterialTheme.colorScheme.onPrimaryContainer,
                    indicatorColor=MaterialTheme.colorScheme.primaryContainer,
                    selectedTextColor=MaterialTheme.colorScheme.primary,
                    unselectedIconColor=MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor=MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
