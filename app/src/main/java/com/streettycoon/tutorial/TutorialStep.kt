package com.streettycoon.tutorial

/**
 * Defines individual tutorial steps
 * Each step guides the player through a specific game mechanic
 */
sealed class TutorialStep(
    val id: String,
    val title: String,
    val message: String,
    val targetViewId: Int? = null
) {

    /**
     * Phase 1: Tap Mechanics (30 seconds)
     */
    object TapIntro : TutorialStep(
        id = "tap_intro",
        title = "Welcome to Street Tycoon!",
        message = "Tap to serve customers and earn money!"
    )

    object TapButton : TutorialStep(
        id = "tap_button",
        title = "Tap to Earn",
        message = "Tap the button to serve customers. Each tap earns you money!"
    )

    object FirstTaps : TutorialStep(
        id = "first_taps",
        title = "Great Job!",
        message = "Keep tapping! Try to earn ₹10"
    )

    /**
     * Phase 2: Helper Introduction (1 minute)
     */
    object HelperIntro : TutorialStep(
        id = "helper_intro",
        title = "Hire Helpers",
        message = "Helpers earn money automatically, even when you're not tapping!"
    )

    object HireFirstHelper : TutorialStep(
        id = "hire_first_helper",
        title = "Hire Your First Helper",
        message = "Tap the 'Hire Helper' button to get passive income"
    )

    object HelperEarnings : TutorialStep(
        id = "helper_earnings",
        title = "Passive Income",
        message = "Your helper earns ₹5 per second automatically. You can keep tapping for even more!"
    )

    /**
     * Phase 3: Upgrade System (1 minute)
     */
    object UpgradeIntro : TutorialStep(
        id = "upgrade_intro",
        title = "Upgrades",
        message = "Upgrade your stall to earn MORE per tap!"
    )

    object FirstUpgrade : TutorialStep(
        id = "first_upgrade",
        title = "Your First Upgrade",
        message = "Tap the upgrade button to increase your tap income by 50%"
    )

    object UpgradeComplete : TutorialStep(
        id = "upgrade_complete",
        title = "Upgraded!",
        message = "Great! Now you earn more per tap. Keep upgrading to grow faster!"
    )

    /**
     * Phase 4: Zone Unlock Hint (30 seconds)
     */
    object ZoneHint : TutorialStep(
        id = "zone_hint",
        title = "Unlock New Zones",
        message = "Complete upgrades to unlock new zones like Beach, Park, and Mall!"
    )

    object ZoneProgress : TutorialStep(
        id = "zone_progress",
        title = "Zone Progress",
        message = "You need 5 upgrades to unlock the Beach Zone. Keep playing!"
    )

    /**
     * Phase 5: Advanced Features (Progressive)
     */
    object FamilyIntro : TutorialStep(
        id = "family_intro",
        title = "Family System",
        message = "You've built a successful business! Now you can grow your family and manage expenses"
    )

    object CharacterIntro : TutorialStep(
        id = "character_intro",
        title = "Hire Characters",
        message = """
            Hire characters to boost your business:
            • Chef: +50% tap income
            • Manager: -20% upgrade costs
            • Staff: +40% passive income
        """.trimIndent()
    )

    object TutorialComplete : TutorialStep(
        id = "tutorial_complete",
        title = "Tutorial Complete!",
        message = "You're ready to build your street empire! Good luck!"
    )

    companion object {
        /**
         * Get all tutorial steps in order
         */
        fun getAllSteps(): List<TutorialStep> = listOf(
            TapIntro,
            TapButton,
            FirstTaps,
            HelperIntro,
            HireFirstHelper,
            HelperEarnings,
            UpgradeIntro,
            FirstUpgrade,
            UpgradeComplete,
            ZoneHint,
            ZoneProgress,
            TutorialComplete
        )

        /**
         * Get core tutorial steps (essential for new players)
         */
        fun getCoreSteps(): List<TutorialStep> = listOf(
            TapIntro,
            TapButton,
            FirstTaps,
            HelperIntro,
            HireFirstHelper,
            UpgradeIntro,
            FirstUpgrade,
            TutorialComplete
        )

        /**
         * Get advanced tutorial steps (shown progressively)
         */
        fun getAdvancedSteps(): List<TutorialStep> = listOf(
            FamilyIntro,
            CharacterIntro
        )
    }
}
