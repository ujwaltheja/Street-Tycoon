import plotly.graph_objects as go
import numpy as np
from plotly.subplots import make_subplots

# Game balance formulas from the C++ implementation
def stall_upgrade_cost(base_cost, level):
    """Upgrade cost: baseCost * (1.15^level)"""
    return base_cost * (1.15 ** level)

def helper_hire_cost(base_cost, helper_count):
    """Helper cost: baseCost * (1.3^helperCount)"""
    return base_cost * (1.3 ** helper_count)

def income_multiplier(level):
    """Income multiplier: 1 + (level - 1) * 0.5"""
    return 1.0 + (level - 1) * 0.5

# Stall base values (from game_state.cpp)
stall_types = {
    "Tea": {"base_income": 10, "tap_income": 1.0},
    "Dosa": {"base_income": 25, "tap_income": 2.5},
    "Momos": {"base_income": 50, "tap_income": 5.0},
    "Juice": {"base_income": 15, "tap_income": 1.5}
}

# Calculate progression data
levels = np.arange(1, 21)  # Level 1 to 20

# Create subplots
fig = make_subplots(
    rows=2, cols=2,
    subplot_titles=(
        'Stall Upgrade Costs by Level',
        'Helper Hire Costs',
        'Income Multiplier by Level',
        'ROI: Upgrade Cost vs Income Gain'
    ),
    specs=[[{"secondary_y": False}, {"secondary_y": False}],
           [{"secondary_y": False}, {"secondary_y": False}]]
)

colors = ['#1FB8CD', '#DB4545', '#2E8B57', '#D2BA4C']

# Chart 1: Upgrade costs by level for each stall type
for i, (stall_name, values) in enumerate(stall_types.items()):
    base_cost = values["base_income"] * 10
    costs = [stall_upgrade_cost(base_cost, level) for level in levels]

    fig.add_trace(
        go.Scatter(
            x=levels, y=costs,
            name=stall_name,
            mode='lines+markers',
            line=dict(color=colors[i], width=2),
            marker=dict(size=4)
        ),
        row=1, col=1
    )

# Chart 2: Helper hire costs (using Tea stall as example)
tea_base = stall_types["Tea"]["base_income"] * 20
helper_counts = np.arange(0, 11)  # 0 to 10 helpers
helper_costs = [helper_hire_cost(tea_base, count) for count in helper_counts]

fig.add_trace(
    go.Bar(
        x=helper_counts, y=helper_costs,
        name='Helper Cost',
        marker=dict(color='#1FB8CD'),
        text=[f'₹{int(c)}' for c in helper_costs],
        textposition='outside'
    ),
    row=1, col=2
)

# Chart 3: Income multiplier by level
multipliers = [income_multiplier(level) for level in levels]

fig.add_trace(
    go.Scatter(
        x=levels, y=multipliers,
        name='Income Multiplier',
        mode='lines+markers',
        line=dict(color='#2E8B57', width=3),
        marker=dict(size=6),
        fill='tozeroy',
        fillcolor='rgba(46, 139, 87, 0.2)'
    ),
    row=2, col=1
)

# Chart 4: ROI Analysis - Cost vs Income for Tea stall
tea_base_income = stall_types["Tea"]["base_income"]
upgrade_costs = [stall_upgrade_cost(tea_base_income * 10, level) for level in levels]
income_gains = [(income_multiplier(level) * tea_base_income) for level in levels]

fig.add_trace(
    go.Scatter(
        x=levels, y=upgrade_costs,
        name='Upgrade Cost',
        mode='lines',
        line=dict(color='#DB4545', width=2)
    ),
    row=2, col=2
)

fig.add_trace(
    go.Scatter(
        x=levels, y=income_gains,
        name='Income/sec',
        mode='lines',
        line=dict(color='#2E8B57', width=2),
        yaxis='y2'
    ),
    row=2, col=2
)

# Update layout
fig.update_layout(
    title_text="Street Tycoon: Game Progression & Balance Curves",
    showlegend=True,
    height=900,
    plot_bgcolor='white'
)

# Update x-axes
fig.update_xaxes(title_text="Level", gridcolor='#E5E5E5', row=1, col=1)
fig.update_xaxes(title_text="Helper Count", gridcolor='#E5E5E5', row=1, col=2)
fig.update_xaxes(title_text="Level", gridcolor='#E5E5E5', row=2, col=1)
fig.update_xaxes(title_text="Level", gridcolor='#E5E5E5', row=2, col=2)

# Update y-axes
fig.update_yaxes(title_text="Cost (₹)", gridcolor='#E5E5E5', row=1, col=1)
fig.update_yaxes(title_text="Cost (₹)", gridcolor='#E5E5E5', row=1, col=2)
fig.update_yaxes(title_text="Multiplier", gridcolor='#E5E5E5', row=2, col=1)
fig.update_yaxes(title_text="Cost (₹)", gridcolor='#E5E5E5', row=2, col=2)

# Save
fig.write_image('progression_curves.png', width=1400, height=900)
fig.write_image('progression_curves.svg', format='svg')

print("✓ Progression curves chart created")
