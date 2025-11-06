package com.streettycoon.ui.theme

import androidx.compose.ui.graphics.Color

// ==================== PRIMARY COLORS ====================
object Colors {
    // Green Theme (Default)
    val GreenPrimary = Color(0xFF2E7D32)
    val GreenOnPrimary = Color(0xFFFFFFFF)
    val GreenPrimaryContainer = Color(0xFFA5D6A7)
    val GreenOnPrimaryContainer = Color(0xFF1B5E20)
    
    // Orange Theme (Alternative)
    val OrangePrimary = Color(0xFFF57C00)
    val OrangeOnPrimary = Color(0xFFFFFFFF)
    val OrangePrimaryContainer = Color(0xFFFFE0B2)
    val OrangeOnPrimaryContainer = Color(0xFFE65100)
    
    // Blue Theme (Alternative)
    val BluePrimary = Color(0xFF1976D2)
    val BlueOnPrimary = Color(0xFFFFFFFF)
    val BluePrimaryContainer = Color(0xFFBBDEFB)
    val BlueOnPrimaryContainer = Color(0xFF0D47A1)
    
    // ==================== SECONDARY COLORS ====================
    val SecondaryGreen = Color(0xFF558B2F)
    val TertiaryGreen = Color(0xFF2E7D32)
    val SecondaryContainer = Color(0xFFC1E1A6)
    val OnSecondaryContainer = Color(0xFF33691E)
    
    // ==================== NEUTRAL COLORS ====================
    val NeutralBackground = Color(0xFFFBFDF7)
    val NeutralSurface = Color(0xFFFBFDF7)
    val NeutralSurfaceVariant = Color(0xFFDCE4D9)
    val OutlineVariant = Color(0xFFCDD9CF)
    
    // ==================== STATUS COLORS ====================
    val SuccessGreen = Color(0xFF4CAF50)
    val WarningOrange = Color(0xFFFFC107)
    val ErrorRed = Color(0xFFE53935)
    val InfoBlue = Color(0xFF2196F3)
    
    // ==================== GAME-SPECIFIC COLORS ====================
    val CurrencyGold = Color(0xFFFFD700)
    val CurrencyRupee = Color(0xFF2E7D32)
    val ComboYellow = Color(0xFFFFCB2E)
    val LockedGray = Color(0xFF9E9E9E)
    val UnlockedGreen = Color(0xFF66BB6A)
    
    // ==================== SEMI-TRANSPARENT VARIATIONS ====================
    val OverlayDark = Color(0x80000000)
    val OverlayLight = Color(0x80FFFFFF)
    val RippleColor = GreenPrimary.copy(alpha = 0.08f)
}