package compress.joshattic.us.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme

// ── Android 16 dark surface tokens ───────────────────────────────────────────
val Android16DarkBackground           = Color(0xFF111318)
val Android16DarkSurface              = Color(0xFF111318)
val Android16DarkSurfaceContainer     = Color(0xFF1E2027)
val Android16DarkSurfaceContainerHigh = Color(0xFF282A33)
val Android16DarkSurfaceContainerLow  = Color(0xFF181A20)
val Android16DarkOnSurface            = Color(0xFFE2E2E9)
val Android16DarkOnSurfaceVariant     = Color(0xFF9DA3B4)
val Android16DarkOutline              = Color(0xFF444754)
val Android16DarkOutlineVariant       = Color(0xFF2E303B)

// ── Android 16 light surface tokens ──────────────────────────────────────────
val Android16LightBackground           = Color(0xFFEBF0F8)
val Android16LightSurface              = Color(0xFFEBF0F8)
val Android16LightSurfaceContainer     = Color(0xFFDFE5F2)
val Android16LightSurfaceContainerHigh = Color(0xFFD4DEED)
val Android16LightSurfaceContainerHighest = Color(0xFFC7D4E8)
val Android16LightSurfaceContainerLow  = Color(0xFFF1F5FC)
val Android16LightOnSurface            = Color(0xFF191C20)
val Android16LightOnSurfaceVariant     = Color(0xFF43474E)
val Android16LightOutline              = Color(0xFF73777F)
val Android16LightOutlineVariant       = Color(0xFFC3C7D0)

// ── App Icon & Brand Colors ──────────────────────────────────────────────────
val A16BadgeAppGreen        = Color(0xFF0A7F82)   // Exact #6FA450 requested
val A16BadgeAppGreenOnBadge = Color(0xFFFFFFFF)   // Clean white icon on green

val CrisperTealOnDark = Color(0xFF4FD8B8)

// ── Crisper teal theme (names kept from the original palette)
// ── Harmonized Green Theme Color Tokens (Fallback for non-dynamic / Android <= 11) ──
val CompressorLightGreenBackground           = Color(0xFFEEF5F5)
val CompressorLightGreenSurface              = Color(0xFFEEF5F5)
val CompressorLightGreenSurfaceContainerLow  = Color(0xFFF4FAFA)
val CompressorLightGreenSurfaceContainer     = Color(0xFFE1EEEE)
val CompressorLightGreenSurfaceContainerHigh = Color(0xFFD6E6E6)
val CompressorLightGreenSurfaceContainerHighest = Color(0xFFCBDEDE)
val CompressorLightGreenOnSurface            = Color(0xFF141D1D)
val CompressorLightGreenOnSurfaceVariant     = Color(0xFF3E4949)
val CompressorLightGreenOutline              = Color(0xFF6E7979)
val CompressorLightGreenOutlineVariant       = Color(0xFFBEC8C8)

val CompressorDarkGreenBackground            = Color(0xFF0F1414)
val CompressorDarkGreenSurface               = Color(0xFF0F1414)
val CompressorDarkGreenSurfaceContainerLow   = Color(0xFF171C1C)
val CompressorDarkGreenSurfaceContainer      = Color(0xFF1B2020)
val CompressorDarkGreenSurfaceContainerHigh  = Color(0xFF252B2B)
val CompressorDarkGreenSurfaceContainerHighest = Color(0xFF303636)
val CompressorDarkGreenOnSurface             = Color(0xFFDDE4E3)
val CompressorDarkGreenOnSurfaceVariant      = Color(0xFFBEC8C7)
val CompressorDarkGreenOutline               = Color(0xFF889392)
val CompressorDarkGreenOutlineVariant        = Color(0xFF3E4949)

val CompressorLightPrimaryContainer      = Color(0xFFBDF0E4)
val CompressorLightOnPrimaryContainer    = Color(0xFF00201A)
val CompressorLightSecondary             = Color(0xFF4A6360)
val CompressorLightOnSecondary           = Color(0xFFFFFFFF)
val CompressorLightSecondaryContainer    = Color(0xFFCCE8E3)
val CompressorLightOnSecondaryContainer  = Color(0xFF05201C)
val CompressorLightTertiary              = Color(0xFF416278)
val CompressorLightOnTertiary            = Color(0xFFFFFFFF)
val CompressorLightTertiaryContainer     = Color(0xFFC7E7FF)
val CompressorLightOnTertiaryContainer    = Color(0xFF001E2F)

val CompressorDarkPrimaryContainer       = Color(0xFF005047)
val CompressorDarkOnPrimaryContainer     = Color(0xFFBDF0E4)
val CompressorDarkSecondary              = Color(0xFFB1CCC7)
val CompressorDarkOnSecondary            = Color(0xFF1C3532)
val CompressorDarkSecondaryContainer     = Color(0xFF324B48)
val CompressorDarkOnSecondaryContainer   = Color(0xFFCCE8E3)
val CompressorDarkTertiary               = Color(0xFFA9CBE3)
val CompressorDarkOnTertiary             = Color(0xFF0E3447)
val CompressorDarkTertiaryContainer      = Color(0xFF274A5F)
val CompressorDarkOnTertiaryContainer     = Color(0xFFC7E7FF)


// ── Android 16 Expressive Category Badges (Theme Aware) ──────────────────────
// Display (Amber / Orange)
val A16DisplayBgDark    = Color(0xFF3E2A00)
val A16DisplayIconDark  = Color(0xFFFFB951)
val A16DisplayBgLight   = Color(0xFFFFDDB3)
val A16DisplayIconLight = Color(0xFF2A1700)

// Presets (Blue / Purple)
val A16PresetsBgDark    = Color(0xFF1E2347)
val A16PresetsIconDark  = Color(0xFFB0C4FF)
val A16PresetsBgLight   = Color(0xFFD9E2FF)
val A16PresetsIconLight = Color(0xFF001945)

// Video (Cyan / Teal - Soft Pastel)
val A16VideoBgDark    = Color(0xFF1B3B3B)
val A16VideoIconDark  = Color(0xFF90E0E0)
val A16VideoBgLight   = Color(0xFFC5EDED)
val A16VideoIconLight = Color(0xFF0A3B3B)

// Audio (Pink / Rose)
val A16AudioBgDark    = Color(0xFF3E0A1E)
val A16AudioIconDark  = Color(0xFFFFADD4)
val A16AudioBgLight   = Color(0xFFFFD8EC)
val A16AudioIconLight = Color(0xFF3B0019)

@Composable
fun getCategoryBadgeColors(category: String, isDark: Boolean = isSystemInDarkTheme()): Pair<Color, Color> {
    return when (category) {
        "display" -> if (isDark) Pair(A16DisplayBgDark, A16DisplayIconDark) else Pair(A16DisplayBgLight, A16DisplayIconLight)
        "presets" -> if (isDark) Pair(A16PresetsBgDark, A16PresetsIconDark) else Pair(A16PresetsBgLight, A16PresetsIconLight)
        "video"   -> if (isDark) Pair(A16VideoBgDark, A16VideoIconDark) else Pair(A16VideoBgLight, A16VideoIconLight)
        "audio"   -> if (isDark) Pair(A16AudioBgDark, A16AudioIconDark) else Pair(A16AudioBgLight, A16AudioIconLight)
        else      -> Pair(A16BadgeAppGreen, A16BadgeAppGreenOnBadge)
    }
}