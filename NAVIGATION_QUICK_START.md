# Street-Tycoon Navigation - Quick Start Guide

## Installation

The navigation system is already integrated into your project:
- **Location**: `app/src/main/java/com/streettycoon/ui/components/GameNavigation.kt`
- **Status**: ✅ Compiled and ready to use

---

## 5-Minute Setup

### Step 1: Define Navigation Items

```kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.streettycoon.ui.components.GameNavItem

val gameNavItems = listOf(
    GameNavItem(
        id = "map",
        label = "Map",
        icon = Icons.Default.Public,
        route = "map"
    ),
    GameNavItem(
        id = "stall",
        label = "Stall",
        icon = Icons.Default.Store,
        route = "stall",
        badge = null  // Set to "3" to show notification
    ),
    GameNavItem(
        id = "character",
        label = "Character",
        icon = Icons.Default.Person,
        route = "character"
    ),
    GameNavItem(
        id = "family",
        label = "Family",
        icon = Icons.Default.Group,
        route = "family"
    ),
    GameNavItem(
        id = "shop",
        label = "Shop",
        icon = Icons.Default.ShoppingCart,
        route = "shop"
    )
)
```

### Step 2: Add Navigation State to ViewModel

```kotlin
class GameViewModel : ViewModel() {
    private val _selectedNavigation = MutableStateFlow("map")
    val selectedNavigation = _selectedNavigation.asStateFlow()

    fun navigateTo(itemId: String) {
        _selectedNavigation.value = itemId
        // Handle actual navigation to route
    }
}
```

### Step 3: Use in Your Main Screen

```kotlin
@Composable
fun GameMainScreen(viewModel: GameViewModel) {
    val selectedNav by viewModel.selectedNavigation.collectAsState()

    Scaffold(
        bottomBar = {
            GameBottomNavigation(
                items = gameNavItems,
                selectedItemId = selectedNav,
                onItemSelected = { item ->
                    viewModel.navigateTo(item.id)
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Your screen content goes here
            when (selectedNav) {
                "map" -> MapScreen()
                "stall" -> StallScreen()
                "character" -> CharacterScreen()
                "family" -> FamilyScreen()
                "shop" -> ShopScreen()
            }
        }
    }
}
```

---

## Navigation Patterns

### Pattern A: Bottom Navigation Only

**Best for**: Phones, simple navigation (4-5 main sections)

```kotlin
Scaffold(
    bottomBar = {
        GameBottomNavigation(
            items = gameNavItems,
            selectedItemId = selectedNav,
            onItemSelected = { viewModel.navigateTo(it.id) }
        )
    }
) { paddingValues ->
    // Content
}
```

### Pattern B: Tab Navigation

**Best for**: Section sub-menus (e.g., inventory tabs)

```kotlin
val inventoryTabs = listOf(
    GameNavItem("items", "Items", Icons.Default.Storage, route = "items"),
    GameNavItem("equipment", "Equipment", Icons.Default.Shield, route = "equipment"),
    GameNavItem("consumables", "Consumables", Icons.Default.LocalDining, route = "consumables")
)

Column {
    GameTabNavigation(
        items = inventoryTabs,
        selectedItemId = selectedTab,
        onItemSelected = { /* switch tab */ }
    )

    // Tab content below
}
```

### Pattern C: Drawer + Bottom Navigation

**Best for**: Complex apps (settings, help, social)

```kotlin
var showDrawer by remember { mutableStateOf(false) }

Box {
    Scaffold(
        bottomBar = {
            GameBottomNavigation(
                items = gameNavItems,
                selectedItemId = selectedNav,
                onItemSelected = { /* ... */ }
            )
        }
    ) { paddingValues ->
        // Content
    }

    // Drawer overlay
    if (showDrawer) {
        GameNavigationDrawer(
            items = drawerItems,
            selectedItemId = selectedDrawer,
            onItemSelected = {
                viewModel.navigateTo(it.id)
                showDrawer = false
            },
            header = {
                Text("Player Level", color = Color.White)
            }
        )
    }
}
```

### Pattern D: Floating Action Button for Primary Action

**Best for**: Main action button (Start Game, Begin Event)

```kotlin
Box(modifier = Modifier.fillMaxSize()) {
    // Screen content

    GameFloatingActionButton(
        icon = Icons.Default.PlayArrow,
        label = "Start Game",
        onClick = { viewModel.startGame() },
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp)
    )
}
```

---

## Customization

### Change Colors

Edit in your theme or directly:

```kotlin
// Deep dark background (instead of using Theme defaults)
val DARK_BG = Color(0xFF1A1A1A)
val SELECTED_COLOR = Colors.CurrencyGold      // Gold
val UNSELECTED_COLOR = Color(0xFFB0B0B0)      // Gray

GameBottomNavigation(
    items = items,
    selectedItemId = selected,
    onItemSelected = { onSelect(it) }
    // Colors are hardcoded in component, can override by copy-pasting
)
```

To fully customize, you can copy the component function and modify:

```kotlin
// In your own file or modify GameNavigation.kt
val backgroundColor = Color(0xFF2D2D2D)  // Your custom dark color
val selectedColor = Colors.TealPrimary   // Use teal instead of gold
val unselectedColor = Color(0xFF909090)  // Different gray
```

### Add Badges

Show notification counts:

```kotlin
GameNavItem(
    id = "stall",
    label = "Stall",
    icon = Icons.Default.Store,
    route = "stall",
    badge = "3"  // Shows red badge with "3"
)
```

### Customize Label Text

```kotlin
GameNavItem(
    id = "map",
    label = "Explore",  // Change label
    icon = Icons.Default.Public,
    route = "map"
)
```

---

## Animation Control

All animations are built-in and cannot be disabled, but you can customize timing:

**Pulsing Glow** (1500ms):
- In `GameBottomNavigation()` - line ~70

**Color Transition** (300ms):
- In `GameNavItem()` - line ~135

To change, modify the numbers in your local copy:

```kotlin
// Change from 1500ms to 1000ms
animationSpec = infiniteRepeatable(
    animation = tween(1000, easing = FastOutSlowInEasing),  // Was 1500
    repeatMode = RepeatMode.Reverse
)
```

---

## Safe Areas

Automatically handled by Compose:

```kotlin
Scaffold(
    bottomBar = {
        GameBottomNavigation(...)  // Sits above system nav
    }
)
```

Or manually:

```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .systemBarsPadding()  // Respects safe areas
) {
    // Content
    GameNavigationDrawer(...)  // Respects left inset
}
```

---

## Testing Navigation

### Preview

```kotlin
@Preview
@Composable
fun GameBottomNavigationPreview() {
    GameBottomNavigation(
        items = gameNavItems,
        selectedItemId = "map",
        onItemSelected = { }
    )
}
```

### Unit Test

```kotlin
@Test
fun testNavigation() {
    composeTestRule.setContent {
        GameBottomNavigation(
            items = gameNavItems,
            selectedItemId = "map",
            onItemSelected = { selected ->
                assert(selected.id == "stall")
            }
        )
    }

    composeTestRule
        .onNodeWithContentDescription("Stall")
        .performClick()
}
```

---

## Troubleshooting

### Navigation items not visible
- ✅ Ensure `selectedItemId` matches one of your item IDs
- ✅ Check that `items` list is not empty
- ✅ Verify height is enough (80dp for bottom nav)

### Glow not showing
- ✅ Background must be dark (#1A1A1A or darker)
- ✅ Selected color must be bright (gold, orange)
- ✅ Check that `isSelected` is true

### Animation looks choppy
- ✅ Reduce number of concurrent animations
- ✅ Check device performance
- ✅ Try on real device instead of emulator

### Text overlapping
- ✅ Reduce label text length
- ✅ Increase container size
- ✅ Use icons only (no labels)

---

## Common Modifications

### Icon-Only Navigation

```kotlin
// Remove label from GameNavItem display
// In GameNavItem function, comment out:
// Text(text = item.label, ...)
```

### Custom Spacing

```kotlin
Row(
    horizontalArrangement = Arrangement.spacedBy(8.dp),  // Change from SpaceEvenly
    ...
)
```

### Right-Aligned Drawer

```kotlin
// Copy GameNavigationDrawer and change:
modifier = modifier
    .fillMaxHeight()
    .width(280.dp)
    .align(Alignment.CenterEnd)  // Right side instead of left
```

---

## Full Example: Complete Integration

```kotlin
@Composable
fun GameApp(viewModel: GameViewModel) {
    val selectedNav by viewModel.selectedNavigation.collectAsState()

    Scaffold(
        bottomBar = {
            GameBottomNavigation(
                items = gameNavItems,
                selectedItemId = selectedNav,
                onItemSelected = { item ->
                    viewModel.navigateTo(item.id)
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedNav) {
                "map" -> MapScreen(viewModel)
                "stall" -> StallScreen(viewModel)
                "character" -> CharacterScreen(viewModel)
                "family" -> FamilyScreen(viewModel)
                "shop" -> ShopScreen(viewModel)
                else -> MapScreen(viewModel)  // Default
            }

            // Optional: Floating action button
            GameFloatingActionButton(
                icon = Icons.Default.Add,
                label = "Quick Action",
                onClick = { /* handle */ },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    }
}
```

---

## Next Steps

1. ✅ Copy code examples above
2. ✅ Define your `gameNavItems`
3. ✅ Add to your ViewModel
4. ✅ Use in your main screen
5. ✅ Test on device
6. ✅ Customize colors/sizing as needed

---

## Support

For detailed information, see:
- `NAVIGATION_DESIGN_GUIDE.md` - Complete design specifications
- `GameNavigation.kt` - Source code with inline comments
- `UI_COMPONENTS_QUICK_REFERENCE.md` - General UI component examples

---

**Status**: ✅ Ready for production

Your Street-Tycoon now has a complete, premium navigation system!
