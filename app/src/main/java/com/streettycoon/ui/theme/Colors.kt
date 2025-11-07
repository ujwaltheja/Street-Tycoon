package com.streettycoon.ui.theme

import androidx.compose.ui.graphics.Color

// ==================== WARM ORANGE THEME (HTML Prototype Design) ====================
object Colors {
    // Primary Orange Gradient Colors (from HTML)
    val OrangePrimary = Color(0xFFFF6B35)      // --game-orange
    val OrangeSecondary = Color(0xFFFF8C42)    // Gradient end
    val OrangeAccent = Color(0xFFFFA06E)       // Light orange
    val OrangeOnPrimary = Color(0xFFFFFFFF)
    val OrangePrimaryContainer = Color(0xFFFFE0B2)
    val OrangeOnPrimaryContainer = Color(0xFFE65100)

    // Cream Background Colors (from HTML)
    val CreamBackground = Color(0xFFFFF3E0)    // --color-cream-50
    val CreamSurface = Color(0xFFFFFFFD)       // --color-cream-100
    val CreamLight = Color(0xFFFCFCF9)         // Lightest cream
    val CreamWarm = Color(0xFFFFCC80)          // Warm cream

    // Text Colors (from HTML)
    val TextPrimary = Color(0xFF13343B)        // --color-slate-900
    val TextSecondary = Color(0xFF626C71)      // --color-slate-500
    val TextTertiary = Color(0xFF777C7C)       // --color-gray-400

    // Teal Accent Colors (from HTML)
    val TealPrimary = Color(0xFF21808D)        // --color-teal-500
    val TealLight = Color(0xFF32B8C6)          // --color-teal-300
    val TealDark = Color(0xFF1D7480)           // --color-teal-600
    val TealOnPrimary = Color(0xFFFFFFFF)

    // Status Colors
    val SuccessGreen = Color(0xFF4CAF50)
    val WarningOrange = Color(0xFFF57C00)
    val ErrorRed = Color(0xFFC0152F)           // --color-red-500
    val ErrorLight = Color(0xFFFF5459)         // --color-red-400
    val InfoBlue = Color(0xFF2196F3)

    // Game-Specific Colors (from HTML)
    val CurrencyGold = Color(0xFFFFD700)       // --game-gold
    val ComboYellow = Color(0xFFFFD54F)        // For combo counter
    val LockedGray = Color(0xFF9E9E9E)
    val UnlockedGreen = Color(0xFF66BB6A)

    // Zone Colors (from HTML prototype)
    val ZoneBlue = Color(0xFF64B5F6)
    val ZonePurple = Color(0xFFBA68C8)
    val ZoneGreen = Color(0xFF81C784)
    val ZoneYellow = Color(0xFFFFB74D)
    val ZonePink = Color(0xFFF06292)

    // Card and Border Colors
    val CardBackground = Color(0xFFFFFFFF)
    val CardBorder = Color(0x1F5E5240)         // rgba(94, 82, 64, 0.12) from HTML
    val CardShadow = Color(0x0A000000)         // Subtle shadow
    
    // Neutral colors for Material3 compatibility
    val NeutralSurfaceVariant = Color(0xFFE8E4DF)
    val NeutralBackground = Color(0xFFFCFCF9)
    val OutlineVariant = Color(0xFFCAC4BF)

    // Legacy Green Theme (kept for backwards compatibility)
    val GreenPrimary = Color(0xFF2E7D32)
    val GreenOnPrimary = Color(0xFFFFFFFF)
    val GreenPrimaryContainer = Color(0xFFA5D6A7)
    val GreenOnPrimaryContainer = Color(0xFF1B5E20)

    // Semi-Transparent Variations
    val OverlayDark = Color(0xCC000000)        // Darker overlay for modals
    val OverlayLight = Color(0x80FFFFFF)
    val RippleColor = OrangePrimary.copy(alpha = 0.12f)
    val GlassOverlay = Color(0xEBFFFFFF)       // 92% white for glass cards
}