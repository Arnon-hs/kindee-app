package com.apoorvdarshan.calorietracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.apoorvdarshan.calorietracker.models.MacroValueFormatter
import com.apoorvdarshan.calorietracker.ui.theme.AppColors

@Composable
fun MacroCard(label:String,current:Double,goal:Double,unit:String="g",modifier:Modifier=Modifier,gradientColors:List<Color> = listOf(AppColors.CalorieStart,AppColors.CalorieEnd)) {
    Column(modifier.padding(vertical=8.dp,horizontal=4.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
        Text(label,style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.onSurfaceVariant)
        Text("${MacroValueFormatter.string(current)}$unit",style=MaterialTheme.typography.titleMedium)
        Text(if(goal>0)"of ${MacroValueFormatter.string(goal)}$unit" else "No target",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
