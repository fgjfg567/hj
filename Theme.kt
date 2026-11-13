package sa.pricecompare.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

val Navy = Color(0xFF0D1B2A)
val Orange = Color(0xFFFF6B00)
val Surface0 = Color(0xFFF5F6F8)

private val Colors = lightColorScheme(
    primary = Orange,
    onPrimary = Color.White,
    secondary = Navy,
    background = Surface0,
    surface = Color.White,
    onSurface = Navy
)

@Composable
fun PriceTheme(content: @Composable () -> Unit) {
    // Force RTL for Arabic-first layout.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(colorScheme = Colors, content = content)
    }
}
