package com.apoorvdarshan.calorietracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private fun lightColors(themeColor: AppThemeColor) = lightColorScheme(
    primary = themeColor.start,
    primaryContainer = androidx.compose.ui.graphics.Color(0xFFD9E8C9),
    onPrimaryContainer = AppColors.OnLight,
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFFD9E8C9),
    onSecondaryContainer = AppColors.OnLight,
    tertiaryContainer = androidx.compose.ui.graphics.Color(0xFFD9E8C9),
    onTertiaryContainer = AppColors.OnLight,
    outlineVariant = AppColors.DividerLight,
    error = androidx.compose.ui.graphics.Color(0xFFAB3720),
    surfaceTint = themeColor.start,
    onPrimary = AppColors.OnDark,
    secondary = themeColor.start,
    onSecondary = AppColors.OnDark,
    tertiary = themeColor.start,
    onTertiary = AppColors.OnDark,
    background = AppColors.AppBackgroundLight,
    onBackground = AppColors.OnLight,
    surface = AppColors.AppCardLight,
    onSurface = AppColors.OnLight,
    surfaceVariant = AppColors.AppCardLight,
    onSurfaceVariant = AppColors.MutedLight,
    outline = AppColors.DividerLight
)

private fun darkColors(themeColor: AppThemeColor) = darkColorScheme(
    primary = AppColors.Calorie,
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF304B38),
    onPrimaryContainer = AppColors.OnDark,
    secondaryContainer = androidx.compose.ui.graphics.Color(0xFF304B38),
    onSecondaryContainer = AppColors.OnDark,
    tertiaryContainer = androidx.compose.ui.graphics.Color(0xFF304B38),
    onTertiaryContainer = AppColors.OnDark,
    outlineVariant = AppColors.DividerDark,
    error = androidx.compose.ui.graphics.Color(0xFFFFB4A3),
    surfaceTint = AppColors.Calorie,
    onPrimary = AppColors.AppBackgroundDark,
    secondary = AppColors.Calorie,
    onSecondary = AppColors.AppBackgroundDark,
    tertiary = AppColors.Calorie,
    onTertiary = AppColors.AppBackgroundDark,
    background = AppColors.AppBackgroundDark,
    onBackground = AppColors.OnDark,
    surface = AppColors.AppCardDark,
    onSurface = AppColors.OnDark,
    surfaceVariant = AppColors.AppCardDark,
    onSurfaceVariant = AppColors.MutedDark,
    outline = AppColors.DividerDark
)

@Composable
fun FudAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeColor: AppThemeColor = AppThemeColor.FUD_PINK,
    content: @Composable () -> Unit
) {
    AppColors.setThemeColor(themeColor, darkTheme)
    val colorScheme = if (darkTheme) darkColors(themeColor) else lightColors(themeColor)
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
