import plotly.graph_objects as go
from plotly.subplots import make_subplots
import numpy as np

# Zone unlock costs (from game_state.cpp)
zones = {
    "Marketplace": 0,
    "Temple Street": 500,
    "Tech Park": 2000,
    "Beach Road": 5000,
    "Old City": 10000,
    "Downtown": 25000
}

# Helper ROI calculation
def calculate_helper_roi(base_income, level, helper_income_per_sec):
    """
    Calculate how long it takes for a helper to pay for itself
    Helper cost: baseIncome * 20 * (1.3^helperCount)
    Helper income: baseIncome * 0.5 * (1 + (level - 1) * 0.5)
    """
    results = []
    for helper_num in range(10):
        cost = base_income * 20 * (1.3 ** helper_num)
        income_per_sec = (base_income * 0.5) * (1 + (level - 1) * 0.5)
        payback_time_sec = cost / income_per_sec if income_per_sec > 0 else 0
        payback_time_min = payback_time_sec / 60
        results.append({
            'helper_num': helper_num + 1,
            'cost': cost,
            'income_per_sec': income_per_sec,
            'payback_minutes': payback_time_min
        })
    return results

# Create subplots
fig = make_subplots(
    rows=2, cols=2,
    subplot_titles=(
        'Zone Unlock Progression',
        'Helper Payback Time (ROI) - Tea Stall Level 5',
        'Cumulative Zone Unlock Costs',
        'Early Game Balance: First Hour Progression'
    ),
    specs=[[{"type": "bar"}, {"type": "scatter"}],
           [{"type": "scatter"}, {"type": "scatter"}]]
)

# Chart 1: Zone unlock costs
zone_names = list(zones.keys())
zone_costs = list(zones.values())
colors_zones = ['#2E8B57', '#1FB8CD', '#D2BA4C', '#DB4545', '#964325', '#944454']

fig.add_trace(
    go.Bar(
        x=zone_names,
        y=zone_costs,
        marker=dict(color=colors_zones),
        text=[f'₹{c}' if c > 0 else 'FREE' for c in zone_costs],
        textposition='outside',
        showlegend=False
    ),
    row=1, col=1
)

# Chart 2: Helper ROI for Tea stall at level 5
tea_base_income = 10
level = 5
roi_data = calculate_helper_roi(tea_base_income, level, tea_base_income * 0.5)

helper_numbers = [r['helper_num'] for r in roi_data]
payback_times = [r['payback_minutes'] for r in roi_data]

fig.add_trace(
    go.Scatter(
        x=helper_numbers,
        y=payback_times,
        mode='lines+markers',
        line=dict(color='#1FB8CD', width=3),
        marker=dict(size=8),
        name='Payback Time',
        showlegend=False
    ),
    row=1, col=2
)

# Chart 3: Cumulative zone costs
cumulative_costs = np.cumsum(zone_costs)

fig.add_trace(
    go.Scatter(
        x=zone_names,
        y=cumulative_costs,
        mode='lines+markers',
        line=dict(color='#DB4545', width=3),
        marker=dict(size=10),
        fill='tozeroy',
        fillcolor='rgba(219, 69, 69, 0.2)',
        name='Cumulative Cost',
        showlegend=False
    ),
    row=2, col=1
)

# Chart 4: Early game progression simulation (first hour)
# Assuming: Start with ₹100, 1 Tea stall, tap every 5 seconds, upgrade at levels 2,3,4
time_minutes = np.arange(0, 61)  # 0 to 60 minutes
cash = [100]  # Starting cash
taps_per_minute = 12  # Tap every 5 seconds

current_cash = 100
current_level = 1
tap_income = 1.0

for minute in range(1, 61):
    # Manual tapping income
    income_this_minute = tap_income * taps_per_minute * (1 + (current_level - 1) * 0.5)
    current_cash += income_this_minute

    # Auto-upgrade logic (simplified)
    upgrade_cost = (tap_income * 10) * (1.15 ** current_level)
    if current_cash >= upgrade_cost and current_level < 10:
        current_cash -= upgrade_cost
        current_level += 1

    cash.append(current_cash)

fig.add_trace(
    go.Scatter(
        x=time_minutes,
        y=cash,
        mode='lines',
        line=dict(color='#2E8B57', width=2),
        fill='tozeroy',
        fillcolor='rgba(46, 139, 87, 0.2)',
        name='Cash Progression',
        showlegend=False
    ),
    row=2, col=2
)

# Update layout
fig.update_layout(
    title_text="Street Tycoon: Game Balance Analysis",
    height=900,
    plot_bgcolor='white',
    showlegend=False
)

# Update axes
fig.update_xaxes(title_text="Zone", tickangle=45, gridcolor='#E5E5E5', row=1, col=1)
fig.update_xaxes(title_text="Helper Number", gridcolor='#E5E5E5', row=1, col=2)
fig.update_xaxes(title_text="Zone", tickangle=45, gridcolor='#E5E5E5', row=2, col=1)
fig.update_xaxes(title_text="Time (minutes)", gridcolor='#E5E5E5', row=2, col=2)

fig.update_yaxes(title_text="Unlock Cost (₹)", gridcolor='#E5E5E5', row=1, col=1)
fig.update_yaxes(title_text="Payback Time (minutes)", gridcolor='#E5E5E5', row=1, col=2)
fig.update_yaxes(title_text="Total Cost (₹)", gridcolor='#E5E5E5', row=2, col=1)
fig.update_yaxes(title_text="Cash (₹)", gridcolor='#E5E5E5', row=2, col=2)

# Add annotations
fig.add_annotation(
    text="Lower is better - helpers pay for themselves faster",
    xref="x2", yref="y2",
    x=5, y=max(payback_times) * 0.8,
    showarrow=True,
    arrowhead=2,
    ax=-50, ay=-40
)

# Save
fig.write_image('game_balance.png', width=1400, height=900)
fig.write_image('game_balance.svg', format='svg')

print("✓ Game balance analysis chart created")
