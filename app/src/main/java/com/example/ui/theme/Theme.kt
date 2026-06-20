package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = LavenderActive,
    secondary = AccentPurpleBg,
    tertiary = AmberSoon,
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF2C2B2F),
    onPrimary = LavenderText,
    onSecondary = AccentPurpleText,
    onBackground = ProfessionalBg,
    onSurface = ProfessionalBg
  )

private val LightColorScheme =
  lightColorScheme(
    primary = AccentPurple,
    secondary = InactiveText,
    tertiary = AmberSoon,
    background = ProfessionalBg,
    surface = NeutralBg,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = HardText,
    onSurface = HardText,
    outline = ProfessionalBorder
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
