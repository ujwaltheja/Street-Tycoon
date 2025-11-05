# Street Tycoon - Design & Visualization Guide

This document explains all the design visualizations and charts created for the Street Tycoon project.

## Table of Contents

1. [Architecture Visualizations](#architecture-visualizations)
2. [Game Balance Charts](#game-balance-charts)
3. [Player Analytics](#player-analytics)
4. [Performance & Technical](#performance--technical)
5. [Monetization](#monetization)
6. [Localization](#localization)
7. [Generating Charts](#generating-charts)

---

## Architecture Visualizations

### 1. Enhanced Architecture Diagram
**File**: `architecture_enhanced.png`
**Script**: `chart_architecture_enhanced.py`

**Purpose**: Shows the complete technical architecture with timing details and data flow.

**Key Components**:
- **UI Layer**: Jetpack Compose screens (Map, Stall, Shop)
- **ViewModel**: GameViewModel with tick loop (100ms) and auto-save (30s)
- **Data Layer**: GameRepository and Room database
- **Simulation Layer**: Kotlin wrapper, JNI bridge, C++ core
- **Timing Info**: Shows critical latencies and update frequencies

**Insights**:
- Tick loop runs every 100ms for smooth passive income updates
- Auto-save runs every 30 seconds to persist game state
- JNI overhead is ~1-2ms per call
- UI reacts to StateFlow changes from ViewModel

### 2. Original Architecture (Basic)
**File**: `street_tycoon_architecture.png`
**Script**: `chart_script.py`

**Purpose**: Simplified architecture overview showing main components.

---

## Game Balance Charts

### 3. Progression Curves
**File**: `progression_curves.png`
**Script**: `chart_progression_curves.py`

**Purpose**: Visualizes exponential progression for upgrades, helpers, and income.

**Charts Included**:
1. **Stall Upgrade Costs by Level**: Shows exponential growth formula `baseCost * (1.15^level)` for all 4 stall types
2. **Helper Hire Costs**: Cost increases with each helper `baseCost * (1.3^helperCount)`
3. **Income Multiplier by Level**: Shows how income scales `1 + (level - 1) * 0.5`
4. **ROI Analysis**: Compares upgrade cost vs income gain to show payback time

**Key Balance Decisions**:
- 15% cost increase per level (gentle exponential curve)
- 30% cost increase per helper (encourages spreading across stalls)
- 50% income boost per level (meaningful upgrades)

### 4. Game Balance Analysis
**File**: `game_balance.png`
**Script**: `chart_game_balance.py`

**Purpose**: Comprehensive game economy analysis.

**Charts Included**:
1. **Zone Unlock Progression**: Free → ₹500 → ₹2000 → ₹5000 → ₹10000 → ₹25000
2. **Helper Payback Time (ROI)**: How quickly each helper pays for itself
3. **Cumulative Zone Costs**: Total investment needed to unlock all zones
4. **Early Game Progression**: First hour cash simulation with upgrades

**Key Insights**:
- First helper pays for itself in ~10-15 minutes
- Later helpers take longer (exponential cost) but provide persistent value
- Zone unlocks create meaningful progression gates
- Early game loop provides satisfying progress in 1-5 minute sessions

### 5. Offline Accrual Formula
**File**: `offline_accrual.png` & `offline_accrual_comparison.png`
**Script**: `chart_offline_accrual.py`

**Purpose**: Shows how offline earnings are calculated and capped.

**Formula**: `min(offlineTime, 4h) × incomePerSec × 0.7`

**Key Features**:
- 4 hour cap prevents time manipulation exploits
- 70% efficiency penalty (vs 100% online) encourages active play
- Scales with player's actual income rate
- Comparison chart shows actual vs theoretical earnings

**Design Rationale**:
- Cap prevents players from just opening app once a day
- 30% penalty rewards active play without punishing casual players
- 4 hours is enough for overnight/work but not exploitable

### 6. Stall Comparison
**File**: `stall_comparison.png` & `stall_progression_by_type.png`
**Script**: `chart_stall_comparison.py`

**Purpose**: Compares the 4 stall types across multiple dimensions.

**Stall Types**:
- **Tea ☕**: Budget tier (₹10/s base, ₹1 tap)
- **Juice 🧃**: Budget tier (₹15/s base, ₹1.5 tap)
- **Dosa 🥞**: Mid-tier (₹25/s base, ₹2.5 tap)
- **Momos 🥟**: Premium (₹50/s base, ₹5 tap)

**Charts Included**:
1. Base passive income comparison
2. Tap income comparison
3. Initial costs (unlock, upgrade, helper) - stacked
4. Efficiency ratio (income/unlock cost)

**Key Insights**:
- Linear progression: each tier ~2x the previous
- Higher tier stalls require more investment but scale better
- All stalls maintain same efficiency ratio (balanced design)
- Progression by type shows income growth with helpers

---

## Player Analytics

### 7. Player Retention Funnel
**File**: `player_funnel.png`
**Script**: `chart_player_funnel.py`

**Purpose**: Shows expected player retention and engagement metrics.

**Funnel Stages**:
- Install: 100%
- Complete Tutorial: 80%
- Play 5 Minutes: 70%
- Unlock 2nd Stall: 60%
- Day 1 Return: 35% ⭐ (target)
- Day 7 Return: 10% ⭐ (target)
- First IAP: 2%

**Additional Charts**:
1. **Session Length Distribution**: Target 1-5 minutes with meaningful progress
2. **Player Progression Timeline**: Key milestones mapped to playtime
3. **Engagement Metrics Targets**: D1 retention, D7 retention, avg session length

**Key Targets** (from MVP requirements):
- D1 Retention >= 35%
- D7 Retention >= 10%
- Average session: 3-5 minutes
- Sessions per day: 4-6

---

## Performance & Technical

### 8. Performance Benchmarks
**File**: `performance_benchmarks.png` & `performance_over_time.png`
**Script**: `chart_performance_benchmarks.py`

**Purpose**: Technical performance targets and monitoring.

**Charts Included**:
1. **Tick Loop Latency**: Target vs max acceptable for each operation
2. **Memory Usage by Device**: 45MB (low-end) to 120MB (high-end)
3. **JNI Call Overhead**: Distribution showing most calls are 1-2ms
4. **FPS Target Gauge**: 60 FPS target

**Performance Targets**:
- Total tick latency: 7ms target, 10ms max
- Native simulation: 0.5ms
- JNI crossing: 1.5ms
- JSON parsing: 2ms
- UI update: 3ms

**Over Time Chart**:
- Shows realistic 60-second session with occasional GC pauses
- Most ticks stay under 10ms threshold
- GC pauses annotated (15-30ms spikes)

**Testing Requirements**:
- Test on 512MB-1GB RAM devices
- Maintain 60 FPS on mid-range devices
- Throttle tick frequency if needed on low-end

---

## Monetization

### 9. Monetization Flow Diagram
**File**: `monetization_flow.png`
**Script**: `chart_monetization_flow.py`

**Purpose**: Shows player journey through monetization touchpoints.

**Flow Stages**:
1. **Free Gameplay**: Core loop is fully accessible
2. **Triggers**: Need boost, want tokens, like game
3. **Monetization Options**:
   - Rewarded Ads (60% of revenue)
   - IAP Token Packs (30%)
   - Cosmetics (10%)
4. **Rewards**: 2x earnings, instant upgrades, tokens, skins
5. **Enhanced Experience**: Returns to gameplay

**Key Principles**:
- ✅ Non-intrusive (player choice)
- ✅ Value-driven (meaningful rewards)
- ✅ Fair pricing
- ❌ No pay-to-win (cosmetics only)
- ❌ No forced ads

### 10. Monetization Model Comparison
**File**: `monetization_chart.png`
**Script**: `chart_script_1.py`

**Purpose**: Industry comparison of monetization approaches.

**Models Compared**:
- Rewarded Ads: 28 games (most common)
- Cosmetic IAPs: 23 games
- Pay-to-Progress IAPs: 17 games
- Subscriptions: 4 games
- Banner/Interstitial Ads: 18 games

**Street Tycoon Choice**: Hybrid model (rewarded ads + cosmetic IAPs)

---

## Localization

### 11. Localization Coverage
**File**: `localization_coverage.png`
**Script**: `chart_localization_fixed.py`

**Purpose**: Shows translation coverage across supported languages.

**Languages**:
- English: 20 strings (100% coverage)
- Hindi (हिन्दी): 20 strings (100% coverage)
- Kannada (ಕನ್ನಡ): 20 strings (100% coverage)

**Coverage**: All core UI strings translated across 3 languages

**Note**: Original `chart_script_2.py` had incorrect pie chart showing 50/25/25% split. Fixed version shows accurate bar chart with actual string counts.

---

## Generating Charts

### Prerequisites

Install required Python packages:

```bash
pip install -r requirements.txt
```

Required packages:
- plotly >= 5.17.0
- pandas >= 2.1.0
- numpy >= 1.24.0
- kaleido >= 0.2.1 (for PNG/SVG export)
- matplotlib >= 3.8.0
- seaborn >= 0.13.0
- pillow >= 10.0.0

### Running Individual Scripts

Each chart has its own Python script:

```bash
# Architecture
python chart_architecture_enhanced.py

# Game Balance
python chart_progression_curves.py
python chart_game_balance.py
python chart_offline_accrual.py
python chart_stall_comparison.py

# Player Analytics
python chart_player_funnel.py

# Performance
python chart_performance_benchmarks.py

# Monetization
python chart_monetization_flow.py
python chart_script_1.py

# Localization
python chart_localization_fixed.py
```

### Batch Generation

Generate all charts at once:

```bash
# Create a script to run all
for script in chart_*.py; do
    echo "Generating $script..."
    python "$script"
done
```

### Output Formats

Each script generates:
- **PNG**: High-resolution raster (for documentation, presentations)
- **SVG**: Vector format (for scaling, editing in design tools)

### Customization

All scripts use consistent color palette:
- Primary: `#1FB8CD` (Teal)
- Secondary: `#DB4545` (Red)
- Accent: `#2E8B57` (Green)
- Neutral: `#D2BA4C` (Gold), `#5D878F` (Gray-blue)
- Dark: `#964325`, `#944454` (Browns)

Modify color values in individual scripts to match branding.

---

## Chart Usage Guide

### For Documentation
- Use PNG files in README.md and markdown docs
- High DPI ensures readability on all screens

### For Presentations
- Use PNG for slideshows
- Use SVG for design tools (Figma, Sketch, etc.)

### For Analysis
- Python scripts are fully customizable
- Modify formulas to test balance changes
- Update data based on analytics

### For Development
- Reference charts during implementation
- Use as acceptance criteria for features
- Validate actual vs expected performance

---

## Design Decisions Explained

### Why Exponential Progression?

Exponential curves (1.15^level, 1.3^helper) create:
- ✅ Long-term progression goals
- ✅ Natural difficulty curve
- ✅ Meaningful choices (which stall to upgrade)
- ✅ Prevents instant completion

### Why 4-Hour Offline Cap?

- ✅ Rewards daily engagement (not once-daily)
- ✅ Prevents time manipulation exploits
- ✅ Balances casual vs hardcore players
- ✅ Enough for overnight but not for days away

### Why 100ms Tick Rate?

- ✅ Smooth passive income updates
- ✅ Low enough latency for responsive gameplay
- ✅ High enough to avoid performance issues
- ✅ Industry standard for idle games

### Why 4 Stall Types?

- ✅ Enough variety for choice
- ✅ Not overwhelming for new players
- ✅ Linear progression is easy to understand
- ✅ Expandable in future updates

---

## Future Enhancements

Potential additional charts for future versions:

1. **A/B Testing Dashboards**: Compare balance changes
2. **Live Analytics Integration**: Real player data overlay
3. **Competitive Analysis**: Compare with similar games
4. **UI/UX Heatmaps**: Screen usage patterns
5. **Retention Cohorts**: Time-based player segments
6. **Revenue Forecasting**: Projected ARPDAU curves

---

## Questions & Feedback

For questions about these visualizations or to request additional charts, please open an issue in the GitHub repository.

**Visualization Principles**:
- Clarity over complexity
- Data-driven design decisions
- Transparent balancing
- Accessible to technical and non-technical stakeholders

---

**Built with ❤️ for the Street Tycoon project**
