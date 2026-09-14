package goonarr.stash

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.ColorUtils
import goonarr.stash.util.LocalStashHapticFeedback
import goonarr.stash.util.StashHapticFeedback
import java.lang.Math.pow
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

// Goonarr theme colors (current default)
private val GoonarrBackground = Color(0xFF0D1426)
private val GoonarrCardBackground = Color(0xFF1A2133)
private val GoonarrSecondaryBackground = Color(0xFF262E40)
private val GoonarrAccent = Color(0xFFFFBA33)
private val GoonarrError = Color(0xFFB00020)

// Stash Blueprint-inspired theme colors
private val StashBluePrimary = Color(0xFF137CBD) // #137CBD
private val StashBlueSecondary = Color(0xFF394B59) // #394B59
private val StashSuccess = Color(0xFF0F9960) // #0F9960
private val StashWarning = Color(0xFFD9822B) // #D9822B
private val StashDanger = Color(0xFFDB3737) // #DB3737
private val StashBackground = Color(0xFF202B33) // #202B33
private val StashSurface = Color(0xFF30404D) // #30404D
private val StashSurfaceVariant = Color(0xFF394B59) // #394B59
private val StashOnBackground = Color(0xFFF5F8FA) // #F5F8FA
private val StashOnSurfaceVariant = Color(0xFFBFCCD6) // #BFCCD6
private val StashLink = Color(0xFF48AFF0) // #48AFF0
private val StashOutline = Color(0xFF414C53) // #414C53

// Public color for use throughout the app
val StashBlue = Color(0xFF2196F3)

// Theme type enumeration
enum class ThemeType {
    GOONARR,
    STASH,
    MATERIAL_YOU
}

val LocalBlurNsfw = staticCompositionLocalOf { false }
val LocalThemeType = staticCompositionLocalOf { ThemeType.GOONARR }

// Design Tokens
object StashTokens {
    // Alpha values
    object Alpha {
        const val CardBackground = 0.15f
        const val CardBorder = 0.5f
        const val TextSecondary = 0.6f
        const val Divider = 0.1f
        const val Overlay = 0.3f
        const val ButtonBackground = 0.4f
    }

    // Spacing
    object Spacing {
        val ContentHorizontal = 16.dp
        val ContentVertical = 8.dp
        val CardPadding = 16.dp
        val SectionSpacing = 32.dp
    }

    // Radius
    object Radius {
        val Card = 12.dp
        val Button = 50.dp
        val Small = 4.dp
        val Medium = 8.dp
    }
}

// Goonarr color schemes (current default)
private val GoonarrDarkColorScheme = darkColorScheme(
    primary = GoonarrAccent,
    secondary = StashBlue,
    tertiary = GoonarrSecondaryBackground,
    background = GoonarrBackground,
    surface = GoonarrCardBackground,
    error = GoonarrError,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.White,
    onSurfaceVariant = Color.White.copy(alpha = 0.75f),
    onSecondaryContainer = Color.White,
    onTertiaryContainer = Color.White
)

private val GoonarrLightColorScheme = lightColorScheme(
    primary = GoonarrAccent,
    secondary = StashBlue,
    tertiary = GoonarrSecondaryBackground,
    background = GoonarrBackground,
    surface = GoonarrCardBackground,
    error = GoonarrError,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.White,
    onSurfaceVariant = Color.White.copy(alpha = 0.75f),
    onSecondaryContainer = Color.White,
    onTertiaryContainer = Color.White
)

// Stash Blueprint color schemes
private val StashDarkColorScheme = darkColorScheme(
    primary = StashBluePrimary,
    secondary = StashBlueSecondary,
    tertiary = StashSuccess,
    background = StashBackground,
    surface = StashSurface,
    surfaceVariant = StashSurfaceVariant,
    error = StashDanger,
    onPrimary = Color.White,
    onSecondary = StashOnBackground,
    onTertiary = Color.White,
    onBackground = StashOnBackground,
    onSurface = StashOnBackground,
    onError = Color.White,
    onSurfaceVariant = StashOnSurfaceVariant,
    onSecondaryContainer = StashOnBackground,
    onTertiaryContainer = Color.White,
    outline = StashOutline
)

private val StashLightColorScheme = lightColorScheme(
    primary = StashBluePrimary,
    secondary = StashBlueSecondary,
    tertiary = StashSuccess,
    background = StashBackground,
    surface = StashSurface,
    surfaceVariant = StashSurfaceVariant,
    error = StashDanger,
    onPrimary = Color.White,
    onSecondary = StashOnBackground,
    onTertiary = Color.White,
    onBackground = StashOnBackground,
    onSurface = StashOnBackground,
    onError = Color.White,
    onSurfaceVariant = StashOnSurfaceVariant,
    onSecondaryContainer = StashOnBackground,
    onTertiaryContainer = Color.White,
    outline = StashOutline
)

// Legacy aliases for backward compatibility
private val DarkColorScheme = GoonarrDarkColorScheme
private val LightColorScheme = GoonarrLightColorScheme

// Set of Material typography styles to start with
val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun StashTheme(
    theme: ThemeType = ThemeType.GOONARR,
    darkTheme: Boolean = isSystemInDarkTheme(),
    blurNsfw: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when (theme) {
        ThemeType.GOONARR -> if (darkTheme) GoonarrDarkColorScheme else GoonarrLightColorScheme
        ThemeType.STASH -> if (darkTheme) StashDarkColorScheme else StashLightColorScheme
        ThemeType.MATERIAL_YOU -> {
            // Material You dynamic colors (requires Android 12+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                // Fallback to Goonarr theme on older devices
                if (darkTheme) GoonarrDarkColorScheme else GoonarrLightColorScheme
            }
        }
    }

    val view = androidx.compose.ui.platform.LocalView.current
    val stashHaptic = remember(view) {
        StashHapticFeedback(view)
    }

    CompositionLocalProvider(
        LocalStashHapticFeedback provides stashHaptic,
        LocalBlurNsfw provides blurNsfw,
        LocalThemeType provides theme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

// Dynamic Colors System
/**
 * Holds a set of dynamic colors generated from a scene's hero color.
 * Includes triadic colors for varied and harmonious UI highlights.
 */
data class StashDynamicColors(
    // The hero color itself
    val primary: Color,
    // Hue + 180
    val complementary: Color,
    // Hue + 120, Saturation * 0.6 (Triadic 1)
    val tertiary: Color,
    // Hue + 240, Saturation * 0.6 (Triadic 2)
    val quaternary: Color,
    val onPrimary: Color = Color.Black
) {
    // Convenience properties for common use cases
    val cardBackground: Color get() = primary.copy(alpha = StashTokens.Alpha.CardBackground)
    val cardBorder: Color get() = primary.copy(alpha = StashTokens.Alpha.CardBorder)
    val buttonBackground: Color get() = primary.copy(alpha = StashTokens.Alpha.ButtonBackground)
}

val LocalStashDynamicColors = staticCompositionLocalOf {
    StashDynamicColors(
        primary = GoonarrAccent,
        // Default fallback
        complementary = StashBlue,
        tertiary = GoonarrSecondaryBackground,
        quaternary = StashBlue.copy(alpha = 0.5f)
    )
}

object StashTheme {
    val colors: StashDynamicColors
        @Composable
        get() = LocalStashDynamicColors.current

    val tokens: StashTokens = StashTokens
}

/**
 * Remembers and calculates dynamic colors based on a hero color.
 * Uses HSL rotations to generate complementary and triadic color schemes.
 * Theme-aware: adjusts default color and saturation based on active theme.
 *
 * @param heroColor The base color to generate the scheme from. Defaults to theme accent.
 */
@Composable
fun rememberSceneColors(heroColor: Color?): StashDynamicColors {
    val themeType = LocalThemeType.current
    val materialTheme = MaterialTheme.colorScheme
    val baseColor = heroColor ?: when (themeType) {
        ThemeType.GOONARR -> GoonarrAccent // Warm amber
        ThemeType.STASH -> StashBluePrimary // Cool blue
        ThemeType.MATERIAL_YOU -> materialTheme.primary // Use Material You primary color
    }

    return remember(baseColor, themeType) {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(
            android.graphics.Color.argb(
                (baseColor.alpha * 255).toInt(),
                (baseColor.red * 255).toInt(),
                (baseColor.green * 255).toInt(),
                (baseColor.blue * 255).toInt()
            ),
            hsl
        )

        // Theme-aware saturation multiplier
        val satMultiplier = when (themeType) {
            ThemeType.GOONARR -> 0.6f // Current behavior - vibrant
            ThemeType.STASH -> 0.5f // Slightly more muted for Blueprint aesthetic
            ThemeType.MATERIAL_YOU -> 0.7f // More vibrant for Material You dynamic colors
        }

        // Complementary: Hue + 180
        val compHsl = hsl.clone()
        compHsl[0] = (compHsl[0] + 180f) % 360f
        val compInt = ColorUtils.HSLToColor(compHsl)
        val complementary = Color(compInt).copy(alpha = baseColor.alpha)

        // Tertiary (Triadic 1): Hue + 120, Sat * multiplier
        val tertHsl = hsl.clone()
        tertHsl[0] = (tertHsl[0] + 120f) % 360f
        tertHsl[1] = (tertHsl[1] * satMultiplier).coerceIn(0f, 1f)
        val tertInt = ColorUtils.HSLToColor(tertHsl)
        val tertiary = Color(tertInt).copy(alpha = baseColor.alpha)

        // Quaternary (Triadic 2): Hue + 240, Sat * multiplier
        val quatHsl = hsl.clone()
        quatHsl[0] = (quatHsl[0] + 240f) % 360f
        quatHsl[1] = (quatHsl[1] * satMultiplier).coerceIn(0f, 1f)
        val quatInt = ColorUtils.HSLToColor(quatHsl)
        val quaternary = Color(quatInt).copy(alpha = baseColor.alpha)

        StashDynamicColors(
            primary = baseColor,
            complementary = complementary,
            tertiary = tertiary,
            quaternary = quaternary
        )
    }
}

/**
 * Calculates a tonal variation of a color by interpolating towards white (lighten) or black (darken).
 *
 * @param fraction The fraction to interpolate (0.0 to 1.0).
 * @param towards The color to interpolate towards. Defaults to Black for darkening.
 */
fun Color.tonal(fraction: Float, towards: Color = Color.Black): Color {
    return lerp(this, towards, fraction)
}

// WCAG 2.1 Contrast Helpers

/**
 * Converts sRGB color component to linear RGB for luminance calculation.
 */
private fun srgbToLinear(c: Float): Double {
    val v = c.toDouble()
    return if (v <= 0.04045) v / 12.92 else ((v + 0.055) / 1.055).pow(2.4)
}

/**
 * Calculates the relative luminance of a color according to WCAG 2.1.
 */
private fun relativeLuminance(color: Color): Double {
    val r = srgbToLinear(color.red)
    val g = srgbToLinear(color.green)
    val b = srgbToLinear(color.blue)
    return 0.2126 * r + 0.7152 * g + 0.0722 * b
}

/**
 * Calculates the contrast ratio between two colors according to WCAG 2.1.
 * Returns a value from 1:1 (no contrast) to 21:1 (maximum contrast).
 */
fun contrastRatio(foreground: Color, background: Color): Double {
    val l1 = relativeLuminance(foreground)
    val l2 = relativeLuminance(background)
    val lighter = kotlin.math.max(l1, l2)
    val darker = kotlin.math.min(l1, l2)
    return (lighter + 0.05) / (darker + 0.05)
}

/**
 * Composites a foreground color over a background color using alpha blending.
 */
fun blendOver(fg: Color, bg: Color): Color {
    val a = fg.alpha
    val outA = a + bg.alpha * (1f - a)
    if (outA <= 0f) return Color.Transparent
    val r = (fg.red * a + bg.red * bg.alpha * (1f - a)) / outA
    val g = (fg.green * a + bg.green * bg.alpha * (1f - a)) / outA
    val b = (fg.blue * a + bg.blue * bg.alpha * (1f - a)) / outA
    return Color(r, g, b, outA)
}

/**
 * Result of contrast-aware color selection containing the background and content colors.
 */
data class ReadableColors(
    val background: Color,
    val content: Color
)

/**
 * Computes a readable on-color (white or black) for a given background color,
 * optionally shifting the background tone to meet WCAG contrast requirements.
 *
 * @param background The background color to evaluate.
 * @param baseSurface The underlying surface color (used if background has alpha).
 * @param minRatio The minimum contrast ratio to target (default 4.5:1 for AA).
 * @return A [ReadableColors] containing the (potentially shifted) background and readable content color.
 */
fun readableOnColor(
    background: Color,
    baseSurface: Color,
    minRatio: Double = 4.5
): ReadableColors {
    val effectiveBg = if (background.alpha < 1f) blendOver(background, baseSurface) else background
    val white = Color.White
    val black = Color.Black

    val whiteRatio = contrastRatio(white, effectiveBg)
    val blackRatio = contrastRatio(black, effectiveBg)
    val on = if (whiteRatio >= blackRatio) white else black

    if (contrastRatio(on, effectiveBg) >= minRatio) {
        return ReadableColors(background = background, content = on)
    }

    val target = if (on == white) black else white
    var shifted = effectiveBg
    var t = 0.0
    while (t <= 0.70) {
        shifted = lerp(effectiveBg, target, t.toFloat())
        if (contrastRatio(on, shifted) >= minRatio) {
            return ReadableColors(background = shifted, content = on)
        }
        t += 0.05
    }

    return ReadableColors(background = if (on == white) black else white, content = on)
}
