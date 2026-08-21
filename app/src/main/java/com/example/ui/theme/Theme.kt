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

private val DarkColorScheme = darkColorScheme(
  primary = MedicalTealDark,
  onPrimary = Color(0xFF042F2E),
  primaryContainer = MedicalTealDarkContainer,
  onPrimaryContainer = Color(0xFFCCFBF1),
  secondary = MedicalBlueDark,
  onSecondary = Color(0xFF082F49),
  secondaryContainer = MedicalBlueDarkContainer,
  onSecondaryContainer = Color(0xFFE0F2FE),
  tertiary = MedicalGreenDark,
  onTertiary = Color(0xFF022C22),
  background = MedicalBackgroundDark,
  onBackground = MedicalOnBackgroundDark,
  surface = MedicalSurfaceDark,
  onSurface = MedicalOnSurfaceDark,
  surfaceVariant = MedicalSurfaceVariantDark,
  onSurfaceVariant = MedicalOnSurfaceVariantDark,
  outline = MedicalOutline,
  error = HealthEmergencyRed
)

private val LightColorScheme = lightColorScheme(
  primary = MedicalTealPrimary,
  onPrimary = MedicalTealOnPrimary,
  primaryContainer = MedicalTealContainer,
  onPrimaryContainer = MedicalTealOnContainer,
  secondary = MedicalBlueSecondary,
  onSecondary = MedicalBlueOnSecondary,
  secondaryContainer = MedicalBlueContainer,
  onSecondaryContainer = MedicalBlueOnContainer,
  tertiary = MedicalGreenTertiary,
  onTertiary = MedicalGreenOnTertiary,
  tertiaryContainer = MedicalGreenContainer,
  onTertiaryContainer = MedicalGreenOnContainer,
  background = MedicalBackground,
  onBackground = MedicalOnBackground,
  surface = MedicalSurface,
  onSurface = MedicalOnSurface,
  surfaceVariant = MedicalSurfaceVariant,
  onSurfaceVariant = MedicalOnSurfaceVariant,
  outline = MedicalOutline,
  error = HealthEmergencyRed
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Healthcare branding works best with custom theme
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

