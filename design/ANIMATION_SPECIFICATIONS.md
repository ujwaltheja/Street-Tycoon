# Street Tycoon Animation Specifications

## Overview
This document defines animation specifications for Street Tycoon premium UI. All animations target 60fps performance on mid-tier devices with graceful degradation for lower-end hardware.

## Animation Philosophy

### Core Principles
1. **Purpose-driven**: Every animation serves a functional purpose (feedback, transition, delight)
2. **Performance-first**: 60fps minimum, optimized for battery life
3. **Accessibility-aware**: Respects reduced motion preferences
4. **Natural motion**: Uses physics-based easing for believable movement
5. **Consistent timing**: Follows Material Motion guidelines

### Motion Duration Guidelines
- **Instant**: 0ms - Immediate state changes
- **Fast**: 100-150ms - Quick feedback (button press, ripple)
- **Normal**: 200-300ms - Standard transitions (screen changes, reveals)
- **Slow**: 400-500ms - Emphasized animations (celebrations, errors)
- **Slower**: 600-800ms - Complex choreography (level up, map unlock)
- **Slowest**: 1000-1200ms - Full-screen transitions, hero animations

## Easing Functions

### Standard Easings
```javascript
// Material Motion Easing Curves
const easings = {
  // Accelerate: Elements leaving the screen
  standard: 'cubic-bezier(0.4, 0.0, 0.2, 1)',
  
  // Decelerate: Elements entering the screen
  decelerate: 'cubic-bezier(0.0, 0.0, 0.2, 1)',
  
  // Accelerate-decelerate: Elements moving on screen
  accelerate: 'cubic-bezier(0.4, 0.0, 1, 1)',
  
  // Spring: Playful, bouncy interactions
  spring: 'cubic-bezier(0.34, 1.56, 0.64, 1)',
  
  // Linear: Continuous animations (spinners, progress)
  linear: 'linear'
}
```

### Custom Easings
```javascript
// Custom easing for specific effects
const customEasings = {
  // Bounce effect for level up
  bounce: 'cubic-bezier(0.68, -0.55, 0.265, 1.55)',
  
  // Overshoot for emphasis
  overshoot: 'cubic-bezier(0.175, 0.885, 0.32, 1.275)',
  
  // Smooth ease for balance counter
  smoothEase: 'cubic-bezier(0.25, 0.46, 0.45, 0.94)'
}
```

## Core Animation Specs

### 1. Balance Count-Up Animation

**Purpose**: Smoothly animate balance changes to provide satisfying feedback

**Trigger**: When wallet balance increases or decreases

**Duration**: 800ms

**Easing**: Custom exponential ease-out

**Implementation**:
```kotlin
// Jetpack Compose implementation
@Composable
fun AnimatedBalance(
    targetBalance: Double,
    modifier: Modifier = Modifier
) {
    var previousBalance by remember { mutableStateOf(0.0) }
    val animatedBalance by animateFloatAsState(
        targetValue = targetBalance.toFloat(),
        animationSpec = tween(
            durationMillis = 800,
            easing = FastOutSlowInEasing
        )
    )
    
    LaunchedEffect(targetBalance) {
        previousBalance = animatedBalance.toDouble()
    }
    
    Text(
        text = formatCurrency(animatedBalance.toDouble()),
        style = MaterialTheme.typography.displayMedium,
        modifier = modifier
    )
}
```

**Keyframes**:
```json
{
  "name": "balance-count-up",
  "duration": 800,
  "easing": "exponential-ease-out",
  "properties": {
    "value": {
      "from": "{{previousBalance}}",
      "to": "{{newBalance}}",
      "interpolation": "exponential"
    },
    "scale": {
      "0%": 1.0,
      "20%": 1.05,
      "100%": 1.0
    },
    "color": {
      "0%": "{{baseColor}}",
      "50%": "{{highlightColor}}",
      "100%": "{{baseColor}}"
    }
  }
}
```

**Visual Effect**:
- Numbers count up smoothly with exponential easing
- Slight scale pulse (1.0 -> 1.05 -> 1.0) for emphasis
- Color flash on significant gains (>100) - green for income, red for expense

---

### 2. Transaction Card Slide

**Purpose**: Smoothly introduce new transactions to the list

**Trigger**: New transaction added to recent activity

**Duration**: 300ms

**Easing**: Decelerate (ease-out)

**Implementation**:
```kotlin
@Composable
fun TransactionItem(
    transaction: Transaction,
    index: Int,
    modifier: Modifier = Modifier
) {
    val offsetY = remember { Animatable(100f) }
    val alpha = remember { Animatable(0f) }
    
    LaunchedEffect(transaction.transactionId) {
        launch {
            offsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 300,
                    delayMillis = index * 50, // Stagger effect
                    easing = FastOutSlowInEasing
                )
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 300)
            )
        }
    }
    
    Card(
        modifier = modifier
            .offset(y = offsetY.value.dp)
            .alpha(alpha.value)
    ) {
        // Transaction content
    }
}
```

**Keyframes**:
```json
{
  "name": "transaction-slide-in",
  "duration": 300,
  "easing": "ease-out",
  "properties": {
    "translateY": {
      "0%": "100%",
      "100%": "0%"
    },
    "opacity": {
      "0%": 0,
      "100%": 1
    }
  },
  "stagger": {
    "enabled": true,
    "delay": 50,
    "maxItems": 5
  }
}
```

**Visual Effect**:
- Card slides in from bottom (translateY: 100% -> 0%)
- Fades in simultaneously (opacity: 0 -> 1)
- Staggered when multiple transactions arrive (50ms delay each)

---

### 3. Level-Up Celebration

**Purpose**: Create a rewarding moment when player levels up

**Trigger**: Player reaches required XP for next level

**Duration**: 1200ms

**Easing**: Spring bounce

**Implementation**:
```kotlin
@Composable
fun LevelUpAnimation(
    newLevel: Int,
    onComplete: () -> Unit
) {
    var animationPhase by remember { mutableStateOf(0) }
    val scale by animateFloatAsState(
        targetValue = when (animationPhase) {
            0 -> 0.8f
            1 -> 1.2f
            else -> 1.0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    val rotation by animateFloatAsState(
        targetValue = when (animationPhase) {
            0 -> 0f
            1 -> 360f
            else -> 360f
        },
        animationSpec = tween(durationMillis = 800)
    )
    
    LaunchedEffect(Unit) {
        delay(100)
        animationPhase = 1
        delay(800)
        animationPhase = 2
        delay(400)
        onComplete()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale)
                .rotate(rotation)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_level_badge),
                contentDescription = "Level Up",
                modifier = Modifier.size(120.dp),
                tint = Color(0xFF9C27B0)
            )
            Text(
                text = "LEVEL $newLevel",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White
            )
        }
        
        // Particle effects
        LevelUpParticles()
    }
}
```

**Keyframes**:
```json
{
  "name": "level-up-celebration",
  "duration": 1200,
  "layers": [
    {
      "name": "badge",
      "properties": {
        "scale": [
          {"time": 0, "value": 0.8, "easing": "spring"},
          {"time": 400, "value": 1.2, "easing": "spring"},
          {"time": 800, "value": 1.0, "easing": "spring"}
        ],
        "rotation": [
          {"time": 0, "value": 0},
          {"time": 800, "value": 360, "easing": "ease-in-out"}
        ],
        "opacity": [
          {"time": 0, "value": 0},
          {"time": 200, "value": 1},
          {"time": 1000, "value": 1},
          {"time": 1200, "value": 0}
        ]
      }
    },
    {
      "name": "particles",
      "count": 20,
      "properties": {
        "translateY": {
          "from": 0,
          "to": -200,
          "duration": 1000,
          "easing": "ease-out"
        },
        "opacity": [
          {"time": 0, "value": 1},
          {"time": 800, "value": 0}
        ],
        "scale": {
          "from": 1.0,
          "to": 0.5,
          "easing": "linear"
        }
      }
    },
    {
      "name": "glow",
      "properties": {
        "scale": {
          "from": 0.5,
          "to": 2.0,
          "duration": 1000,
          "easing": "ease-out"
        },
        "opacity": [
          {"time": 0, "value": 0.8},
          {"time": 1000, "value": 0}
        ]
      }
    }
  ]
}
```

**Visual Effect**:
- Badge scales from 0.8 -> 1.2 -> 1.0 with spring bounce
- Full 360° rotation during scale-up
- Particle burst effect (20 particles radiate outward)
- Pulsing glow effect behind badge
- Screen overlay with semi-transparent black background
- Haptic feedback pulse at peak scale

---

### 4. Map Reveal Animation

**Purpose**: Dramatic reveal of newly unlocked map zones

**Trigger**: Player completes unlock requirements for next zone

**Duration**: 800ms

**Easing**: Ease-out with overshoot

**Implementation**:
```kotlin
@Composable
fun MapNodeUnlock(
    zone: Zone,
    isUnlocking: Boolean
) {
    val scale by animateFloatAsState(
        targetValue = if (isUnlocking) 1.0f else 0.9f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy
        )
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isUnlocking) 1.0f else 0.5f,
        animationSpec = tween(durationMillis = 800)
    )
    
    val glowSize by animateFloatAsState(
        targetValue = if (isUnlocking) 150f else 0f,
        animationSpec = tween(durationMillis = 800)
    )
    
    Box(contentAlignment = Alignment.Center) {
        // Glow effect
        if (isUnlocking) {
            Canvas(modifier = Modifier.size(glowSize.dp)) {
                drawCircle(
                    color = Color(0xFF66BB6A).copy(alpha = 0.3f),
                    radius = glowSize / 2
                )
            }
        }
        
        // Map node
        Card(
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .alpha(alpha),
            colors = CardDefaults.cardColors(
                containerColor = if (zone.isUnlocked) 
                    Color(0xFF66BB6A) 
                else 
                    Color(0xFF9E9E9E)
            )
        ) {
            // Zone content
        }
    }
}
```

**Keyframes**:
```json
{
  "name": "map-reveal",
  "duration": 800,
  "properties": {
    "opacity": [
      {"time": 0, "value": 0},
      {"time": 300, "value": 1}
    ],
    "scale": [
      {"time": 0, "value": 0.9, "easing": "ease-out"},
      {"time": 800, "value": 1.0, "easing": "overshoot"}
    ],
    "filter": {
      "blur": [
        {"time": 0, "value": "8px"},
        {"time": 400, "value": "0px"}
      ]
    }
  },
  "effects": [
    {
      "name": "glow-pulse",
      "duration": 800,
      "properties": {
        "size": {
          "from": 0,
          "to": 150,
          "easing": "ease-out"
        },
        "opacity": {
          "from": 0.8,
          "to": 0,
          "easing": "ease-out"
        }
      }
    },
    {
      "name": "ripple",
      "count": 3,
      "stagger": 200,
      "properties": {
        "scale": {
          "from": 1.0,
          "to": 2.0,
          "duration": 600
        },
        "opacity": {
          "from": 0.5,
          "to": 0,
          "duration": 600
        }
      }
    }
  ]
}
```

**Visual Effect**:
- Node fades in from transparent (opacity: 0 -> 1)
- Scales from 0.9 to 1.0 with slight overshoot
- Blur effect: 8px -> 0px for focus draw
- Pulsing glow emanates from center
- Three ripple waves expand outward
- Color transition: gray -> green
- Confetti particles burst on completion

---

### 5. Button Press Interaction

**Purpose**: Provide immediate tactile feedback for button interactions

**Trigger**: Touch down / touch up on buttons

**Duration**: 150ms (press), 150ms (release)

**Easing**: Ease-in-out

**Implementation**:
```kotlin
@Composable
fun PremiumButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = tween(durationMillis = 150)
    )
    
    Button(
        onClick = {
            // Haptic feedback
            onClick()
        },
        modifier = modifier
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            }
    ) {
        content()
    }
}
```

**Keyframes**:
```json
{
  "name": "button-press",
  "phases": {
    "press": {
      "duration": 150,
      "properties": {
        "scale": {
          "from": 1.0,
          "to": 0.95,
          "easing": "ease-in-out"
        },
        "brightness": {
          "from": 1.0,
          "to": 0.9
        }
      }
    },
    "release": {
      "duration": 150,
      "properties": {
        "scale": {
          "from": 0.95,
          "to": 1.0,
          "easing": "ease-in-out"
        },
        "brightness": {
          "from": 0.9,
          "to": 1.0
        }
      }
    }
  },
  "haptic": {
    "type": "light",
    "timing": "on-press"
  }
}
```

---

### 6. Pulse Animation (Continuous)

**Purpose**: Draw attention to important UI elements or notifications

**Trigger**: Notification badge, new content available, tutorial hints

**Duration**: 1000ms (repeating)

**Easing**: Ease-in-out

**Implementation**:
```kotlin
@Composable
fun PulsingBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Badge(
        modifier = modifier
            .scale(scale)
            .alpha(alpha)
    ) {
        Text(text = count.toString())
    }
}
```

**Keyframes**:
```json
{
  "name": "pulse-continuous",
  "duration": 1000,
  "repeat": "infinite",
  "direction": "alternate",
  "properties": {
    "scale": {
      "from": 1.0,
      "to": 1.05,
      "easing": "ease-in-out"
    },
    "opacity": {
      "from": 1.0,
      "to": 0.7,
      "easing": "ease-in-out"
    }
  }
}
```

---

## Performance Optimization

### GPU Acceleration
All animations use GPU-accelerated properties:
- `translateX/Y/Z`
- `scale`
- `rotation`
- `opacity`

Avoid animating:
- `width/height` (use scale instead)
- `padding/margin` (use translate instead)
- `color` alone (combine with opacity for best performance)

### Reduced Motion Support
```kotlin
@Composable
fun respectReducedMotion(
    animation: AnimationSpec<Float>
): AnimationSpec<Float> {
    val prefersReducedMotion = LocalAccessibilityManager.current?.isReducedMotionEnabled ?: false
    
    return if (prefersReducedMotion) {
        snap() // Instant transition
    } else {
        animation
    }
}
```

### Performance Monitoring
```kotlin
// Track animation performance
@Composable
fun MonitoredAnimation(
    animationName: String,
    content: @Composable () -> Unit
) {
    val frameStartTime = remember { mutableStateOf(0L) }
    val frameCount = remember { mutableStateOf(0) }
    
    DisposableEffect(Unit) {
        val choreographer = Choreographer.getInstance()
        val callback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                frameCount.value++
                if (frameTimeNanos - frameStartTime.value > 1_000_000_000) {
                    val fps = frameCount.value
                    Log.d("Animation", "$animationName: $fps fps")
                    frameCount.value = 0
                    frameStartTime.value = frameTimeNanos
                }
                choreographer.postFrameCallback(this)
            }
        }
        choreographer.postFrameCallback(callback)
        
        onDispose {
            choreographer.removeFrameCallback(callback)
        }
    }
    
    content()
}
```

## Testing Animations

### Visual Regression Testing
```kotlin
@Test
fun testBalanceCountUpAnimation() {
    composeTestRule.setContent {
        AnimatedBalance(targetBalance = 1000.0)
    }
    
    composeTestRule.mainClock.advanceTimeBy(400) // Mid-animation
    composeTestRule.onNodeWithText("₹").assertExists()
    
    composeTestRule.mainClock.advanceTimeBy(400) // Complete
    composeTestRule.onNodeWithText("₹1,000").assertExists()
}
```

### Performance Testing
```kotlin
@Test
fun testAnimationPerformance() {
    val frameTimeNanos = mutableListOf<Long>()
    
    composeTestRule.setContent {
        // Record frame times
        DisposableEffect(Unit) {
            val choreographer = Choreographer.getInstance()
            val callback = object : Choreographer.FrameCallback {
                override fun doFrame(frameTimeNanos: Long) {
                    frameTimeNanos.add(frameTimeNanos)
                    if (frameTimeNanos.size < 60) {
                        choreographer.postFrameCallback(this)
                    }
                }
            }
            choreographer.postFrameCallback(callback)
            onDispose { }
        }
        
        // Animation under test
        LevelUpAnimation(newLevel = 10, onComplete = {})
    }
    
    // Verify 60fps (16.67ms per frame)
    val averageFrameTime = frameTimeNanos.zipWithNext { a, b -> 
        (b - a) / 1_000_000.0 
    }.average()
    assertTrue("Animation dropped frames", averageFrameTime < 17.0)
}
```

## Summary

All animations follow these principles:
- **Purposeful**: Enhance usability and provide feedback
- **Performant**: 60fps minimum on mid-tier devices
- **Accessible**: Respect reduced motion preferences
- **Consistent**: Use Material Motion timing and easing
- **Delightful**: Add moments of joy without being distracting

Total animation budget per screen: **< 500ms for critical path interactions**
