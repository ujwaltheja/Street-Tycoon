package com.streettycoon.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ==================== SPACING SCALE ====================
object Spacing {
    val xs = 2.dp
    val sm = 4.dp
    val md = 8.dp
    val lg = 12.dp
    val xl = 16.dp
    val xxl = 24.dp
    val xxxl = 32.dp
    val huge = 48.dp
    val massive = 64.dp
}

// ==================== CORNER RADIUS SYSTEM ====================
val StreetTycoonShapes = Shapes(
    small = RoundedCornerShape(2.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(8.dp)
)

object CornerRadius {
    val Small = 2.dp
    val Medium = 4.dp
    val Large = 8.dp
    val XLarge = 12.dp
    val XXLarge = 16.dp
    val Full = 50.dp
}

// ==================== ELEVATION SYSTEM ====================
object Elevations {
    val Level0 = 0.dp
    val Level1 = 2.dp
    val Level2 = 4.dp
    val Level3 = 8.dp
    val Level4 = 12.dp
    val Level5 = 16.dp
}