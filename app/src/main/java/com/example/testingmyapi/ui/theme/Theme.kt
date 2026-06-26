package com.example.testingmyapi.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.platform.LocalView
import androidx.compose.runtime.SideEffect
import androidx.core.view.WindowCompat

// ============================================================
// ============ DARK MODE (DOMINAN MERAH) ============
// ============================================================
private val DarkColorScheme = darkColorScheme(
    // ===== PRIMARY (MERAH) =====
    primary = Color(0xFFEF5350),              // Merah terang (tombol, icon aktif)
    onPrimary = Color(0xFFFFFFFF),            // ✅ PUTIH (teks di atas primary)
    primaryContainer = Color(0xFF7F0000),     // Merah tua (header card, container)
    onPrimaryContainer = Color(0xFFFFFFFF),   // ✅ PUTIH (teks di atas container)

    // ===== SECONDARY (MERAH MUDA) =====
    secondary = Color(0xFFFF6B6B),            // Merah muda
    onSecondary = Color(0xFFFFFFFF),          // ✅ PUTIH
    secondaryContainer = Color(0xFF4A0A1A),   // Merah sangat tua
    onSecondaryContainer = Color(0xFFFFFFFF), // ✅ PUTIH

    // ===== TERTIARY =====
    tertiary = Color(0xFFD32F2F),             // Merah tua
    onTertiary = Color(0xFFFFFFFF),           // ✅ PUTIH
    tertiaryContainer = Color(0xFF3E0A0A),    // Merah kehitaman
    onTertiaryContainer = Color(0xFFFFFFFF),  // ✅ PUTIH

    // ===== BACKGROUND & SURFACE =====
    background = Color(0xFF0D0D0D),           // Hitam pekat (background)
    onBackground = Color(0xFFFFFFFF),         // ✅ PUTIH (teks di atas background)

    surface = Color(0xFF1A1A1A),              // Hitam abu-abu (card)
    onSurface = Color(0xFFFFFFFF),            // ✅ PUTIH (teks di atas surface)

    surfaceVariant = Color(0xFF2D2D2D),       // Abu-abu gelap
    onSurfaceVariant = Color(0xFFB0B0B0),     // Abu-abu terang (teks sekunder)

    // ===== ERROR =====
    error = Color(0xFFEF5350),                // Merah
    onError = Color(0xFFFFFFFF),              // ✅ PUTIH
    errorContainer = Color(0xFF4A0A1A),
    onErrorContainer = Color(0xFFFFFFFF),     // ✅ PUTIH

    // ===== INVERSE =====
    inverseSurface = Color(0xFFE8E8E8),
    inverseOnSurface = Color(0xFF121212)
)

// ============================================================
// ============ LIGHT MODE (DOMINAN BIRU) ============
// ============================================================
private val LightColorScheme = lightColorScheme(
    // ===== PRIMARY (BIRU) =====
    primary = Color(0xFF1565C0),              // Biru tua (tombol, icon aktif)
    onPrimary = Color(0xFFFFFFFF),            // ✅ PUTIH (teks di atas primary)
    primaryContainer = Color(0xFFBBDEFB),     // Biru sangat muda (header card, container)
    onPrimaryContainer = Color(0xFF000000),   // ✅ HITAM (teks di atas container)

    // ===== SECONDARY (BIRU MUDA) =====
    secondary = Color(0xFF42A5F5),            // Biru terang
    onSecondary = Color(0xFFFFFFFF),          // ✅ PUTIH
    secondaryContainer = Color(0xFFE3F2FD),   // Biru sangat muda
    onSecondaryContainer = Color(0xFF000000), // ✅ HITAM

    // ===== TERTIARY (BIRU LAUT) =====
    tertiary = Color(0xFF0D47A1),             // Biru tua
    onTertiary = Color(0xFFFFFFFF),           // ✅ PUTIH
    tertiaryContainer = Color(0xFFBBDEFB),
    onTertiaryContainer = Color(0xFF000000),  // ✅ HITAM

    // ===== BACKGROUND & SURFACE =====
    background = Color(0xFFF5F9FF),           // Biru abu-abu sangat muda (background)
    onBackground = Color(0xFF000000),         // ✅ HITAM (teks di atas background)

    surface = Color(0xFFFFFFFF),              // Putih (card)
    onSurface = Color(0xFF000000),            // ✅ HITAM (teks di atas surface)

    surfaceVariant = Color(0xFFE8E8E8),
    onSurfaceVariant = Color(0xFF6B6B6B),     // Abu-abu (teks sekunder)

    // ===== ERROR =====
    error = Color(0xFFBA1A1A),                // Merah
    onError = Color(0xFFFFFFFF),              // ✅ PUTIH
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF000000),     // ✅ HITAM

    // ===== INVERSE =====
    inverseSurface = Color(0xFF1E1E1E),
    inverseOnSurface = Color(0xFFFFFFFF)
)

@Composable
fun CharacterAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as androidx.activity.ComponentActivity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}