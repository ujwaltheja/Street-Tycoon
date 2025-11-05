package com.streettycoon.ui.accessibility

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Accessibility utilities for Street Tycoon
 *
 * Provides helper functions and constants for improving accessibility:
 * - Content descriptions for screen readers
 * - Minimum touch target sizes
 * - Semantic properties
 */

/**
 * Minimum touch target size recommended by Material Design (48dp x 48dp)
 */
val MinTouchTargetSize = 48.dp

/**
 * Adds a content description for screen readers
 *
 * @param description The description to announce to screen readers
 */
fun Modifier.contentDescription(description: String): Modifier {
    return this.semantics {
        contentDescription = description
    }
}

/**
 * Ensures the component meets minimum touch target size requirements
 */
fun Modifier.minTouchTarget(): Modifier {
    return this.size(MinTouchTargetSize)
}

/**
 * Format currency for accessibility announcements
 *
 * @param amount The currency amount
 * @param prefix The currency symbol (default: ₹)
 * @return Formatted string for screen readers (e.g., "rupees 1000")
 */
fun formatCurrencyForAccessibility(amount: Double, prefix: String = "₹"): String {
    val currencyName = when (prefix) {
        "₹" -> "rupees"
        "$" -> "dollars"
        else -> "currency"
    }

    return when {
        amount >= 1_000_000_000 -> "$currencyName ${String.format("%.2f", amount / 1_000_000_000)} billion"
        amount >= 1_000_000 -> "$currencyName ${String.format("%.2f", amount / 1_000_000)} million"
        amount >= 1_000 -> "$currencyName ${String.format("%.1f", amount / 1_000)} thousand"
        else -> "$currencyName ${amount.toInt()}"
    }
}

/**
 * Format percentage for accessibility announcements
 *
 * @param value The percentage value (0-100)
 * @return Formatted string (e.g., "75 percent")
 */
fun formatPercentageForAccessibility(value: Float): String {
    return "${value.toInt()} percent"
}

/**
 * Content descriptions for game actions
 */
object GameActionDescriptions {
    const val TAP_SERVE = "Tap to serve customers and earn money"
    const val UPGRADE_STALL = "Upgrade this stall to increase earnings"
    const val HIRE_HELPER = "Hire a helper to automate earnings"
    const val UNLOCK_STALL = "Unlock a new stall"
    const val HIRE_CHARACTER = "Hire a team member"
    const val LEVEL_UP_CHARACTER = "Level up this character"
    const val ASSIGN_CHARACTER = "Assign character to a different stall"
    const val UPGRADE_CATEGORY = "Upgrade spending category"
    const val GET_MARRIED = "Get married to start a family"
    const val HAVE_BABY = "Have a baby to grow your family"
    const val OPEN_SETTINGS = "Open settings menu"
    const val NAVIGATE_BACK = "Navigate back"
}

/**
 * Content descriptions for UI elements
 */
object UIElementDescriptions {
    const val MONEY_COUNTER = "Current money balance"
    const val STALL_CARD = "Stall information card"
    const val CHARACTER_CARD = "Team member card"
    const val FAMILY_MEMBER_CARD = "Family member card"
    const val SPENDING_CATEGORY_CARD = "Spending category card"
    const val PROGRESS_BAR = "Progress indicator"
    const val LEVEL_INDICATOR = "Level indicator"
    const val HAPPINESS_INDICATOR = "Happiness level"
    const val COMBO_COUNTER = "Combo counter"
}

/**
 * Format stall information for accessibility
 *
 * @param stallName The name of the stall
 * @param level The stall level
 * @param earningsPerSecond The earnings rate
 * @return Formatted description
 */
fun formatStallDescription(
    stallName: String,
    level: Int,
    earningsPerSecond: Double
): String {
    return "$stallName, level $level, earning ${formatCurrencyForAccessibility(earningsPerSecond)} per second"
}

/**
 * Format character information for accessibility
 *
 * @param name The character name
 * @param type The character type
 * @param level The character level
 * @param assignedStallName The stall the character is assigned to
 * @return Formatted description
 */
fun formatCharacterDescription(
    name: String,
    type: String,
    level: Int,
    assignedStallName: String?
): String {
    val assignment = assignedStallName?.let { " assigned to $it" } ?: " not assigned"
    return "$name, $type, level $level$assignment"
}

/**
 * Format family member information for accessibility
 *
 * @param name The family member name
 * @param relation The relation (spouse, child, etc.)
 * @param age The age
 * @param happiness The happiness percentage
 * @return Formatted description
 */
fun formatFamilyMemberDescription(
    name: String,
    relation: String,
    age: Int,
    happiness: Float
): String {
    val happinessLevel = when {
        happiness > 75f -> "happy"
        happiness > 50f -> "okay"
        else -> "unhappy"
    }
    return "$name, $relation, $age years old, $happinessLevel"
}

/**
 * Format upgrade button for accessibility
 *
 * @param itemName The item to upgrade
 * @param cost The upgrade cost
 * @param canAfford Whether the player can afford the upgrade
 * @return Formatted description
 */
fun formatUpgradeButtonDescription(
    itemName: String,
    cost: Double,
    canAfford: Boolean
): String {
    val affordability = if (canAfford) "affordable" else "not affordable"
    return "Upgrade $itemName for ${formatCurrencyForAccessibility(cost)}, $affordability"
}

/**
 * Announce state changes for screen readers
 */
object StateAnnouncements {
    fun levelUp(newLevel: Int): String = "Level up! Now level $newLevel"
    fun moneyEarned(amount: Double): String = "Earned ${formatCurrencyForAccessibility(amount)}"
    fun purchaseSuccess(itemName: String): String = "$itemName purchased successfully"
    fun purchaseFailed(reason: String): String = "Purchase failed: $reason"
    fun unlocked(itemName: String): String = "$itemName unlocked"
    fun comboAchieved(comboCount: Int): String = "Combo! $comboCount times"
    fun achievementUnlocked(achievementName: String): String = "Achievement unlocked: $achievementName"
}
