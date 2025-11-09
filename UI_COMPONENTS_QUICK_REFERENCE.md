# Street-Tycoon UI Components - Quick Reference Guide

## Quick Component Usage Examples

### 1. Glossy Cards

#### Basic Glossy Card
```kotlin
GlossyCard(
    backgroundColor = Colors.CardBackground,
    borderColor = Colors.CardBorder
) {
    Text("My Content")
}
```

#### Zone Card with Spotlight
```kotlin
ZoneCardGlossy(
    zoneName = "Downtown",
    zoneRegion = "Manhattan",
    isLocked = false,
    progress = 0.75f,
    backgroundColor = Colors.ZoneBlue,
    borderColor = Colors.TealPrimary,
    onClick = { /* handle click */ }
)
```

#### Combo Counter (Auto-animated glow)
```kotlin
ComboCounter(
    comboCount = comboState,
    isVisible = showCombo
)
```

---

### 2. Enhanced Tap Button

#### Standard Usage
```kotlin
EnhancedTapButton(
    onTap = { handleTap() },
    emoji = "🍵",
    label = "Tap to Serve"
)
```

#### Or Use Stall-Specific Variants
```kotlin
StallTapButton(
    stallType = "TEA",  // "TEA", "DOSA", "MOMOS", "JUICE"
    onTap = { handleTap() }
)
```

**Features:**
- Multi-layer glow effects
- Shimmer shine overlay
- Automatic combo detection
- Haptic feedback on tap
- 180dp button size

---

### 3. Particle Effects

#### Single Floating Particles
```kotlin
// For individual particle effects
FloatingCoinParticle(onComplete = { /* cleanup */ })
FloatingStarParticle(duration = 1500L)
FloatingSparkleParticle(duration = 800L)
FloatingHeartParticle()
FloatingTextParticle(text = "+100", textColor = Colors.SuccessGreen)
```

#### Particle Bursts (Multiple particles)
```kotlin
// Show multiple particles bursting outward
Box {
    CoinBurst(count = 8)          // Coins floating up
    StarBurst(count = 10)         // Stars exploding
    HeartBurst(count = 12)        // Hearts floating
    ConfettiBurst(count = 15)     // Confetti celebration
}
```

#### Full Celebration Effect
```kotlin
@Composable
fun AchievementPopup() {
    Box {
        // Background celebration
        StarBurst(count = 12, duration = 1500L)
        CoinBurst(count = 6, duration = 1200L)

        // Achievement badge
        PulsingRewardBadge(
            text = "Achievement!",
            icon = "🏆",
            backgroundColor = Colors.ComboYellow
        )
    }
}
```

---

### 4. Card Entrance Animations

#### Slide In Effect
```kotlin
SlideInCard(
    duration = 500,
    delay = 0,
    fromLeft = true  // slide from left, false = slide from right
) {
    YourCard()
}
```

#### Scale In Effect (Zoom)
```kotlin
ScaleInCard(
    duration = 500,
    delay = 200  // wait 200ms before starting
) {
    YourCard()
}
```

#### Bounce In Effect
```kotlin
BounceInCard(
    duration = 600,
    delay = 100
) {
    YourCard()
}
```

#### Rotate In Effect (3D)
```kotlin
RotateInCard(
    duration = 600,
    delay = 0
) {
    YourCard()
}
```

#### Float In Effect
```kotlin
FloatInCard(
    duration = 600,
    delay = 150
) {
    YourCard()
}
```

---

### 5. Staggered List Animations

#### Basic List with Stagger
```kotlin
StaggeredEntranceList(
    items = myCharacters,
    itemDelayMs = 100,  // 100ms between each item
    animationDurationMs = 500
) { character, index ->
    CharacterCard(character)
}
```

#### Grid with Stagger
```kotlin
StaggeredEntranceGrid(
    items = myZones,
    columns = 2,
    itemDelayMs = 80,
    animationDurationMs = 500
) { zone, index ->
    ZoneCard(zone)
}
```

---

### 6. Button Animations

#### Enhanced Primary Button (Auto-animated)
```kotlin
PrimaryButton(
    text = "Start Game",
    onClick = { startGame() },
    enabled = true
)
```

**Features:**
- Spring-based press animation
- Shimmer shine effect
- Enhanced shadow on hover
- Haptic feedback
- Disabled state styling

---

### 7. Audio & Haptic Feedback

#### Quick Feedback Triggers
```kotlin
// Success
triggerSuccessFeedback(haptic, hapticManager, playSound)

// Money collected
triggerCoinCollectFeedback(haptic, hapticManager, playSound)

// Level up
triggerLevelUpFeedback(haptic, hapticManager, playSound)

// Achievement
triggerAchievementFeedback(haptic, hapticManager, playSound)

// Combo hit
triggerComboFeedback(haptic, hapticManager, playSound)

// Upgrade done
triggerUpgradeFeedback(haptic, hapticManager, playSound)

// Zone unlocked
triggerZoneUnlockFeedback(haptic, hapticManager, playSound)

// Error
triggerErrorFeedback(haptic, hapticManager, playSound)

// Button tap
triggerTapFeedback(haptic, hapticManager)
```

#### Custom Feedback Coordinator
```kotlin
val coordinator = FeedbackCoordinator(
    hapticManager = hapticManager,
    soundPlayerFn = { soundName -> playSound(soundName) }
)

// Trigger coordinated feedback
coordinator.triggerFeedback(
    hapticType = HapticFeedbackManager.FeedbackType.LEVEL_UP,
    soundName = "level_up",
    delayMs = 100L
)

// Pulsed feedback (multiple taps)
coordinator.triggerPulsedFeedback(
    hapticType = HapticFeedbackManager.FeedbackType.COMBO,
    pulseCount = 3,
    intervalMs = 100L
)
```

#### Intensity Manager
```kotlin
val intensityManager = FeedbackIntensityManager(
    intensity = FeedbackIntensity.NORMAL,
    hapticManager = hapticManager
)

// User changes intensity in settings
intensityManager.setIntensity(FeedbackIntensity.STRONG)

// Feedback scales automatically
intensityManager.performScaledHaptic(
    baseType = HapticFeedbackManager.FeedbackType.TAP_MEDIUM,
    lightType = HapticFeedbackManager.FeedbackType.TAP_LIGHT,
    intenseType = HapticFeedbackManager.FeedbackType.TAP_STRONG
)
```

---

### 8. Helper Components

#### Shimmer Overlay
```kotlin
ShimmerOverlay(
    color = Color.White,
    duration = 3000
)
```

#### Spotlight Effect
```kotlin
SpotlightEffect(
    color = Colors.OrangePrimary,
    intensity = 0.3f,
    duration = 2500,
    alignment = Alignment.TopEnd  // or TopStart, BottomEnd, etc.
)
```

#### Pulsing Glow Modifier
```kotlin
Box(
    modifier = PulsingGlowModifier(
        glowColor = Colors.CurrencyGold,
        duration = 1000,
        initialAlpha = 0.3f,
        targetAlpha = 0.7f
    )
) {
    YourContent()
}
```

#### Pulsing Reward Badge
```kotlin
PulsingRewardBadge(
    text = "5000 XP",
    icon = "⭐",
    backgroundColor = Colors.CurrencyGold,
    textColor = Colors.TextPrimary
)
```

---

## Common Patterns

### Pattern 1: Reward Animation Sequence
```kotlin
@Composable
fun MoneyRewardAnimation(amount: Int) {
    Box {
        // Show coin burst
        CoinBurst(count = 10, duration = 1200L)

        // Floating text with amount
        FloatingTextParticle(
            text = "+$$amount",
            textColor = Colors.SuccessGreen,
            fontSize = 24,
            duration = 1200L
        )

        // Haptic/audio feedback
        LaunchedEffect(Unit) {
            triggerCoinCollectFeedback()
        }
    }
}
```

### Pattern 2: Achievement Unlock
```kotlin
@Composable
fun AchievementUnlockAnimation(achievement: String) {
    Box {
        // Multiple particle bursts
        StarBurst(count = 12)
        CoinBurst(count = 8)
        HeartBurst(count = 10)

        // Pulsing badge
        PulsingRewardBadge(
            text = achievement,
            icon = "🏆",
            backgroundColor = Colors.ComboYellow
        )

        // Feedback
        LaunchedEffect(Unit) {
            triggerAchievementFeedback()
        }
    }
}
```

### Pattern 3: Combo Celebration
```kotlin
@Composable
fun ComboMilestone(comboCount: Int) {
    Box {
        StarBurst(count = 15, duration = 1500L)

        FloatingTextParticle(
            text = "x$comboCount COMBO!",
            textColor = Colors.ComboYellow,
            fontSize = 28,
            duration = 1500L
        )

        LaunchedEffect(Unit) {
            triggerComboFeedback()
        }
    }
}
```

### Pattern 4: List Animation
```kotlin
@Composable
fun CharactersList(characters: List<Character>) {
    StaggeredEntranceList(
        items = characters,
        itemDelayMs = 100,
        animationDurationMs = 500
    ) { character, _ ->
        CharacterCard(character)
    }
}
```

### Pattern 5: Enhanced Screen Transition
```kotlin
@Composable
fun GameScreenTransition(content: @Composable () -> Unit) {
    SlideLeftTransition(duration = 500) {
        content()
    }
}
```

---

## Tips & Best Practices

### Performance Tips
1. **Use `rememberInfiniteTransition` for continuous animations**
   ```kotlin
   val infiniteTransition = rememberInfiniteTransition(label = "my_animation")
   ```

2. **Limit particle count** - Keep < 50 active particles per screen
3. **Use spring animations sparingly** - Only for user interactions
4. **Test on mid-tier devices** - Snapdragon 665 or similar

### Animation Tips
1. **Stagger delays for visual flow** - 80-150ms between items
2. **Use FastOutSlowInEasing for entrances** - Natural deceleration
3. **Keep duration 300-600ms** - Feels snappy without being jarring
4. **Add haptic feedback for confirmation** - Reinforces actions

### Accessibility Tips
1. **Test all components with TalkBack enabled**
2. **Ensure 4.5:1 color contrast ratio**
3. **Keep button sizes >= 48x48dp** (our buttons are 180dp - great!)
4. **Provide semantic labels for animations**

---

## File Locations

| Component | File | Status |
|-----------|------|--------|
| GlossyCard | GameUIComponents.kt | Enhanced ✅ |
| ComboCounter | GameUIComponents.kt | Enhanced ✅ |
| ZoneCardGlossy | GameUIComponents.kt | Enhanced ✅ |
| Shimmer/Spotlight | GameUIComponents.kt | New ✅ |
| EnhancedTapButton | EnhancedTapButton.kt | Enhanced ✅ |
| PrimaryButton | StreetTycoonButtons.kt | Enhanced ✅ |
| Particle Effects | AnimatedParticleEffects.kt | New ✅ |
| Card Animations | AnimatedCardEntrances.kt | New ✅ |
| Haptic/Audio | AudioHapticFeedback.kt | New ✅ |

---

## Summary

These components provide a complete set of tools for creating rich, engaging UI with:
- ✅ Premium visual effects (gloss, glow, shimmer, spotlight)
- ✅ Smooth animations (spring, stagger, entrance effects)
- ✅ Multi-sensory feedback (audio + haptic coordination)
- ✅ Game-like polish and responsiveness
- ✅ Accessibility and performance optimized

**Ready to integrate into your screens!**
