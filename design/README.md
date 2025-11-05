# Street Tycoon - Design Assets & Visualizations

This directory contains all design visualizations, charts, and documentation for the Street Tycoon project.

## 📁 Contents

### Documentation
- **DESIGN_GUIDE.md** - Comprehensive guide explaining all visualizations
- **requirements.txt** - Python dependencies for chart generation
- **README.md** - This file

### Chart Scripts (Python)
All scripts use Plotly for interactive visualizations:

| Script | Purpose | Output Files |
|--------|---------|--------------|
| `chart_architecture_enhanced.py` | Enhanced architecture with timing | `architecture_enhanced.png/svg` |
| `chart_progression_curves.py` | Upgrade costs, income scaling | `progression_curves.png/svg` |
| `chart_game_balance.py` | Zone costs, helper ROI, early game | `game_balance.png/svg` |
| `chart_offline_accrual.py` | Offline earnings formula | `offline_accrual.png/svg`, `offline_accrual_comparison.png/svg` |
| `chart_stall_comparison.py` | Stall type analysis | `stall_comparison.png/svg`, `stall_progression_by_type.png/svg` |
| `chart_player_funnel.py` | Retention & engagement | `player_funnel.png/svg` |
| `chart_monetization_flow.py` | Monetization flow diagram | `monetization_flow.png/svg` |
| `chart_performance_benchmarks.py` | Performance targets | `performance_benchmarks.png/svg`, `performance_over_time.png/svg` |
| `chart_localization_fixed.py` | Translation coverage | `localization_coverage.png/svg` |
| `chart_script_1.py` | Industry monetization comparison | `monetization_chart.png/svg` |

### Legacy Scripts
- `chart_script.py` - Original basic architecture diagram
- `chart_script_2.py` - Original localization pie chart (replaced by fixed version)

### Generated Images
All charts are generated in both PNG (raster) and SVG (vector) formats for maximum flexibility.

## 🚀 Quick Start

### Install Dependencies

```bash
pip install -r requirements.txt
```

### Generate All Charts

```bash
./generate_all_charts.sh
```

Or manually:

```bash
python chart_architecture_enhanced.py
python chart_progression_curves.py
python chart_game_balance.py
# ... etc
```

### View Results

Charts are saved in this directory as PNG and SVG files.

## 📊 Chart Categories

### 1. Architecture (Technical)
- Enhanced architecture with timing details
- Original basic architecture

### 2. Game Balance
- Progression curves (exponential growth formulas)
- Game balance analysis (zones, helpers, ROI)
- Offline accrual calculations
- Stall type comparisons

### 3. Player Analytics
- Retention funnel (D1, D7 targets)
- Session length distribution
- Player progression timeline
- Engagement metrics

### 4. Performance
- Tick latency benchmarks
- Memory usage by device tier
- JNI call overhead
- FPS targets
- Performance over time simulation

### 5. Monetization
- Monetization flow diagram
- Industry comparison (rewarded ads, IAP, etc.)
- Revenue mix breakdown

### 6. Localization
- Translation coverage (English, Hindi, Kannada)

## 🎨 Design Decisions Visualized

All charts are based on actual game code and formulas:

- **Upgrade costs**: `baseCost * (1.15^level)` from `game_state.cpp`
- **Helper costs**: `baseCost * (1.3^helperCount)` from `game_state.cpp`
- **Income multiplier**: `1 + (level - 1) * 0.5` from `game_state.cpp`
- **Offline accrual**: `min(time, 4h) × income × 0.7` from `game_simulation.cpp`

## 🛠️ Customization

### Modify Chart Data

Edit the Python scripts to adjust:
- Colors (consistent palette defined in each script)
- Formulas (test balance changes)
- Ranges (zoom into specific levels/times)
- Annotations (add notes, highlights)

### Export Formats

Each script generates both formats:
- **PNG**: Use in documentation, presentations (1200-1400px width)
- **SVG**: Use in design tools, scalable graphics

### Color Palette

Consistent across all charts:
```python
colors = {
    'primary': '#1FB8CD',     # Teal
    'secondary': '#DB4545',   # Red
    'accent': '#2E8B57',      # Green
    'gold': '#D2BA4C',
    'gray': '#5D878F',
    'brown1': '#964325',
    'brown2': '#944454'
}
```

## 📋 Issues Found & Fixed

### Original Issues
1. ❌ **Localization chart** (`chart_script_2.py`): Showed misleading 50/25/25% pie chart
2. ❌ **Missing progression curves**: No visualization of exponential formulas
3. ❌ **Missing game balance charts**: No ROI, zone progression, early game analysis
4. ❌ **Architecture too basic**: No timing details, tick loop, coroutines
5. ❌ **No offline accrual viz**: Key mechanic not visualized
6. ❌ **No performance benchmarks**: Technical targets not shown
7. ❌ **No requirements.txt**: Dependencies not specified

### Fixes Applied
✅ All issues addressed with new comprehensive charts
✅ Accurate data based on actual game code
✅ Complete documentation in DESIGN_GUIDE.md
✅ Batch generation script for convenience

## 📖 Documentation

For detailed explanations of each chart, design decisions, and how to interpret the visualizations, see **DESIGN_GUIDE.md**.

## 🔄 Regenerating Charts

After code changes (balance tweaks, formula updates), regenerate charts:

```bash
# Update formulas in Python scripts
vim chart_progression_curves.py

# Regenerate
python chart_progression_curves.py

# Or regenerate all
./generate_all_charts.sh
```

## 🤝 Contributing

When adding new visualizations:

1. Follow naming convention: `chart_descriptive_name.py`
2. Generate both PNG and SVG
3. Use consistent color palette
4. Add documentation to DESIGN_GUIDE.md
5. Update this README with new chart info

## 📦 Dependencies

- **plotly** - Interactive charts
- **pandas** - Data manipulation
- **numpy** - Numerical calculations
- **kaleido** - PNG/SVG export
- **matplotlib** - Additional plotting
- **seaborn** - Statistical visualizations
- **pillow** - Image processing

## 🐛 Troubleshooting

### Kaleido Issues
If PNG/SVG export fails:
```bash
pip install --upgrade kaleido
```

### Missing Fonts
Some systems may need additional fonts for proper rendering:
```bash
# Ubuntu/Debian
sudo apt-get install fonts-dejavu-core

# macOS
# System fonts should work by default
```

### Permission Denied
Make generation script executable:
```bash
chmod +x generate_all_charts.sh
```

## 📝 License

These visualizations are part of the Street Tycoon project. Copyright (c) 2024.

---

**For questions or suggestions, open an issue in the main repository.**
