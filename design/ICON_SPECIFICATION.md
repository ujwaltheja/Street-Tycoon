# Street Tycoon Icon Set Specification

## Overview
This document defines the SVG icon set for Street Tycoon game UI. All icons follow Material Design principles with a consistent 24x24dp base size and can scale to 48dp for larger touch targets.

## Icon Categories

### Financial Icons

#### Income Icon (₹ Rupee Symbol)
```svg
<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
  <path fill="#4CAF50" d="M7 15h2c0 1.1.9 2 2 2h2c1.1 0 2-.9 2-2s-.9-2-2-2h-2c-1.1 0-2-.9-2-2s.9-2 2-2h2c1.1 0 2 .9 2 2h2c0-1.66-1.34-3-3-3V5h-2v3c-1.66 0-3 1.34-3 3 0 1.66 1.34 3 3 3h2c.55 0 1 .45 1 1s-.45 1-1 1h-2c-.55 0-1-.45-1-1H7c0 1.66 1.34 3 3 3v3h2v-3c1.66 0 3-1.34 3-3s-1.34-3-3-3h-2c-.55 0-1-.45-1-1s.45-1 1-1h2c.55 0 1 .45 1 1h2c0-1.66-1.34-3-3-3V5h-2v3c-1.66 0-3 1.34-3 3s1.34 3 3 3z"/>
</svg>
```
**Usage**: Income transactions, earnings display, cash flow positive
**Color**: Green (#4CAF50)
**States**: Default, Active (brighter green), Disabled (gray)

#### Spend Icon (Cart with Minus)
```svg
<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
  <path fill="#C0152F" d="M7 18c-1.1 0-1.99.9-1.99 2S5.9 22 7 22s2-.9 2-2-.9-2-2-2zM1 2v2h2l3.6 7.59-1.35 2.45c-.16.28-.25.61-.25.96 0 1.1.9 2 2 2h12v-2H7.42c-.14 0-.25-.11-.25-.25l.03-.12.9-1.63h7.45c.75 0 1.41-.41 1.75-1.03l3.58-6.49c.08-.14.12-.31.12-.48 0-.55-.45-1-1-1H5.21l-.94-2H1zm16 16c-1.1 0-1.99.9-1.99 2s.89 2 1.99 2 2-.9 2-2-.9-2-2-2zM8 10h8v2H8v-2z"/>
</svg>
```
**Usage**: Expense transactions, purchases, cash flow negative
**Color**: Red (#C0152F)
**States**: Default, Active (darker red), Disabled (gray)

#### Save Icon (Piggy Bank)
```svg
<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
  <path fill="#FF8C42" d="M16 6c-2.21 0-4 1.79-4 4h-2c0-3.31 2.69-6 6-6v2zm-8 4c0-4.42 3.58-8 8-8v2c-3.31 0-6 2.69-6 6H8zm12-6c1.1 0 2 .9 2 2v8c0 1.1-.9 2-2 2h-2v2c0 1.1-.9 2-2 2H8c-1.1 0-2-.9-2-2v-2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2h2V2h12v2h2zm-2 10V6H6v8h12z"/>
</svg>
```
**Usage**: Savings goals, wallet balance, reserve funds
**Color**: Orange (#FF8C42)
**States**: Default, Full (gold color), Empty (gray)

#### Invest Icon (Growth Chart)
```svg
<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
  <path fill="#2196F3" d="M16 6l2.29 2.29-4.88 4.88-4-4L2 16.59 3.41 18l6-6 4 4 6.3-6.29L22 12V6z"/>
</svg>
```
**Usage**: Investments, asset growth, passive income
**Color**: Blue (#2196F3)
**States**: Default, Up (green tint), Down (red tint)

#### Loan Icon (Handshake)
```svg
<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
  <path fill="#9C27B0" d="M17.55 11.2l-.23-.23-.35-.35-.53-.53L15 8.66l-.23-.23-.35-.35-.53-.53-1.54-1.53.53-.53.53-.53.53-.53.53-.53.53-.53.77.77.23.23.35.35.53.53L17 7.03l1.54 1.53-1.54 1.53-.53.53-.35.35-.23.23-.34.34zm-12.1 0l.23-.23.35-.35.53-.53.53-.53.53-.53.77-.77.53.53.53.53.53.53.53.53 1.54 1.53-1.54 1.53-.53.53-.35.35-.23.23-1.54 1.53-1.54-1.53-.53-.53-.35-.35-.23-.23-.34-.34z"/>
  <path fill="#9C27B0" d="M12 14c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2zm0-10c-4.41 0-8 3.59-8 8s3.59 8 8 8 8-3.59 8-8-3.59-8-8-8z"/>
</svg>
```
**Usage**: Loan offers, debt, borrowed funds
**Color**: Purple (#9C27B0)
**States**: Default, Active (debt owed), Paid off (gray with checkmark)

### Character & Staff Icons

#### Staff Icon (Person with Badge)
```svg
<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
  <path fill="#21808D" d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4zm8-8h4v4h-4v-4z"/>
  <circle fill="#FFD700" cx="22" cy="6" r="2"/>
</svg>
```
**Usage**: Helper characters, staff management, employee hiring
**Color**: Teal (#21808D) with gold badge
**States**: Available, Hired (green outline), Max capacity (gray)

### Progression Icons

#### Level Icon (Star Badge)
```svg
<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
  <path fill="#9C27B0" d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/>
  <circle fill="#FFFFFF" cx="12" cy="12" r="5" opacity="0.3"/>
  <text x="12" y="15" text-anchor="middle" fill="#9C27B0" font-size="8" font-weight="bold">LV</text>
</svg>
```
**Usage**: Player level display, level-up notification
**Color**: Purple (#9C27B0) with white center
**States**: Default, Level up (animated glow), Max level (gold)

#### Unlock Icon (Unlocked Padlock)
```svg
<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
  <path fill="#66BB6A" d="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6h1.9c0-1.71 1.39-3.1 3.1-3.1 1.71 0 3.1 1.39 3.1 3.1v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2zm0 12H6V10h12v10zm-6-3c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2z"/>
</svg>
```
**Usage**: Zone unlocks, feature unlocks, achievement unlocks
**Color**: Green (#66BB6A)
**States**: Locked (gray with closed padlock), Unlocking (animated), Unlocked

#### XP Icon (Lightning Bolt)
```svg
<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
  <path fill="#FFD700" d="M7 2v11h3v9l7-12h-4l4-8z"/>
</svg>
```
**Usage**: Experience points, XP gains, progression indicator
**Color**: Gold (#FFD700)
**Animation**: Pulse on XP gain

## Icon Naming Convention

All icons follow this naming pattern:
```
ic_[category]_[name]_[size].svg
```

Examples:
- `ic_financial_income_24.svg`
- `ic_financial_spend_24.svg`
- `ic_staff_character_48.svg`
- `ic_progression_level_24.svg`

## Icon States

Each icon supports multiple states:
1. **Default**: Normal appearance
2. **Active**: Highlighted, often with brighter color
3. **Disabled**: Grayed out at 40% opacity
4. **Hover**: 10% lighter on desktop
5. **Pressed**: 10% darker with scale(0.95) transform

## Color Palette Reference

| Category | Default | Active | Disabled |
|----------|---------|--------|----------|
| Income | #4CAF50 | #66BB6A | #9E9E9E |
| Expense | #C0152F | #E53935 | #9E9E9E |
| Save | #FF8C42 | #FFB74D | #9E9E9E |
| Invest | #2196F3 | #42A5F5 | #9E9E9E |
| Loan | #9C27B0 | #AB47BC | #9E9E9E |
| Staff | #21808D | #32B8C6 | #9E9E9E |
| Level | #9C27B0 | #AB47BC | #9E9E9E |
| Unlock | #66BB6A | #81C784 | #9E9E9E |

## Accessibility

All icons must:
- Maintain 3:1 contrast ratio against background (AA standard for graphical objects)
- Have clear semantic meaning
- Include descriptive content descriptions for screen readers
- Scale properly from 16dp to 48dp without losing clarity
- Support dynamic color theming (Material You)

## Animation Guidelines

Icons can be animated in these contexts:
1. **Entry**: Fade in + scale from 0.8 to 1.0 over 200ms
2. **Exit**: Fade out + scale from 1.0 to 0.8 over 150ms
3. **Active State**: Pulse scale from 1.0 to 1.1 and back over 400ms
4. **Interaction**: Press scale to 0.95 over 100ms, release to 1.0 over 150ms
5. **Notification**: Bounce animation (scale 1.0 -> 1.2 -> 1.0) over 500ms

## Implementation

### Kotlin/Compose
```kotlin
@Composable
fun FinancialIcon(
    type: FinancialIconType,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    contentDescription: String? = null
) {
    Icon(
        painter = painterResource(
            when (type) {
                FinancialIconType.INCOME -> R.drawable.ic_financial_income_24
                FinancialIconType.EXPENSE -> R.drawable.ic_financial_spend_24
                FinancialIconType.SAVE -> R.drawable.ic_financial_save_24
                FinancialIconType.INVEST -> R.drawable.ic_financial_invest_24
                FinancialIconType.LOAN -> R.drawable.ic_financial_loan_24
            }
        ),
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
}
```

### Usage Example
```kotlin
// Income transaction icon
FinancialIcon(
    type = FinancialIconType.INCOME,
    modifier = Modifier.size(24.dp),
    tint = Colors.SuccessGreen,
    contentDescription = "Income received"
)

// Animated level up icon
val scale by animateFloatAsState(
    targetValue = if (isLevelingUp) 1.2f else 1.0f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
)

Icon(
    painter = painterResource(R.drawable.ic_progression_level_24),
    contentDescription = "Level ${currentLevel}",
    modifier = Modifier
        .size(48.dp)
        .scale(scale),
    tint = Colors.LevelPurple
)
```

## File Organization

```
app/src/main/res/drawable/
├── financial/
│   ├── ic_financial_income_24.xml
│   ├── ic_financial_spend_24.xml
│   ├── ic_financial_save_24.xml
│   ├── ic_financial_invest_24.xml
│   └── ic_financial_loan_24.xml
├── staff/
│   └── ic_staff_character_24.xml
└── progression/
    ├── ic_progression_level_24.xml
    ├── ic_progression_unlock_24.xml
    └── ic_progression_xp_24.xml
```

## Export Guidelines

When exporting from design tools:
1. Use 24x24dp base size
2. Export as SVG with minified output
3. Remove unnecessary metadata
4. Optimize paths with SVGO
5. Test at multiple sizes (16, 24, 32, 48dp)
6. Verify contrast ratios
7. Test with screen readers

## Future Additions

Planned icons for future releases:
- Asset-specific icons (tea, dosa, momos, juice stalls)
- Family icons (marriage, children, housing)
- Achievement badges
- Seasonal event icons
- Social sharing icons
