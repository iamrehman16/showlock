package io.github.iamrehman16.showlock

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

// Direction A, "Midnight + Amber". Ground and Amber are mirrored in res/values/colors.xml.
val Ground = Color(0xFF0E1116)
val Surface = Color(0xFF1A1F27)
val Amber = Color(0xFFF5B83D)
val TextPrimary = Color(0xFFE8EAED)
val TextSecondary = Color(0xFFB5BCC5)

private val colors = darkColorScheme(
    primary = Amber,
    onPrimary = Ground,
    background = Ground,
    onBackground = TextPrimary,
    surface = Ground,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    surfaceContainerHigh = Surface,
)

// Static weights cut from the Manrope variable font (third_party/manrope/README.md);
// variation settings on a resource font were not applied on the test phone.
private val Manrope = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold),
)

private val typography = Typography().run {
    fun TextStyle.manrope() = copy(fontFamily = Manrope)
    copy(
        displayLarge = displayLarge.manrope(),
        displayMedium = displayMedium.manrope(),
        displaySmall = displaySmall.manrope(),
        headlineLarge = headlineLarge.manrope(),
        headlineMedium = headlineMedium.manrope(),
        headlineSmall = headlineSmall.manrope(),
        titleLarge = titleLarge.manrope(),
        titleMedium = titleMedium.manrope(),
        titleSmall = titleSmall.manrope(),
        bodyLarge = bodyLarge.manrope(),
        bodyMedium = bodyMedium.manrope(),
        bodySmall = bodySmall.manrope(),
        labelLarge = labelLarge.manrope(),
        labelMedium = labelMedium.manrope(),
        labelSmall = labelSmall.manrope(),
    )
}

@Composable
fun ShowLockTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = colors, typography = typography, content = content)
}
