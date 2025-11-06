package com.streettycoon.ui.animations

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing

// ==================== DURATION CONSTANTS ====================
object AnimationDurations {
    const val SHORT = 150          // Quick feedback animations
    const val MEDIUM = 300         // UI state changes
    const val LONG = 500           // Screen transitions
    const val EXTRA_LONG = 800     // Elaborate sequences
    const val INFINITE_LOOP = 2000 // Background animations
}

// ==================== EASING FUNCTIONS ====================
object EasingFunctions {
    val EaseOutCubic: Easing = CubicBezierEasing(0.215f, 0.61f, 0.355f, 1.0f)
    val EaseInCubic: Easing = CubicBezierEasing(0.55f, 0.055f, 0.675f, 0.19f)
    val EaseInOutCubic: Easing = CubicBezierEasing(0.645f, 0.045f, 0.355f, 1.0f)
    val EaseInOutQuad: Easing = CubicBezierEasing(0.455f, 0.03f, 0.515f, 0.955f)
}

// ==================== SPRINGSPEC FOR INTERACTIVE ELEMENTS ====================
object SpringSpecs {
    val DefaultSpring = androidx.compose.animation.core.spring<Float>(
        dampingRatio = 0.7f,
        stiffness = 500f
    )
    
    val BouncySpring = androidx.compose.animation.core.spring<Float>(
        dampingRatio = 0.6f,
        stiffness = 800f
    )
    
    val SmoothSpring = androidx.compose.animation.core.spring<Float>(
        dampingRatio = 0.95f,
        stiffness = 200f
    )
}