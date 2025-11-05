import plotly.graph_objects as go
from plotly.subplots import make_subplots
import numpy as np

# Stall data from game_state.cpp
stalls = {
    "Tea ☕": {
        "base_income": 10,
        "tap_income": 1.0,
        "tier": "Budget",
        "color": "#1FB8CD"
    },
    "Juice 🧃": {
        "base_income": 15,
        "tap_income": 1.5,
        "tier": "Budget",
        "color": "#D2BA4C"
    },
    "Dosa 🥞": {
        "base_income": 25,
        "tap_income": 2.5,
        "tier": "Mid-tier",
        "color": "#DB4545"
    },
    "Momos 🥟": {
        "base_income": 50,
        "tap_income": 5.0,
        "tier": "Premium",
        "color": "#2E8B57"
    }
}

# Calculate key metrics for each stall type
stall_names = list(stalls.keys())
base_incomes = [s["base_income"] for s in stalls.values()]
tap_incomes = [s["tap_income"] for s in stalls.values()]
unlock_costs = [s["base_income"] * 5 for s in stalls.values()]
upgrade_costs_l1 = [s["base_income"] * 10 for s in stalls.values()]
helper_costs = [s["base_income"] * 20 for s in stalls.values()]
colors = [s["color"] for s in stalls.values()]

# Create subplots
fig = make_subplots(
    rows=2, cols=2,
    subplot_titles=(
        'Base Passive Income per Second',
        'Tap Income (Manual)',
        'Initial Costs Comparison',
        'Income Efficiency (Income/Unlock Cost Ratio)'
    ),
    specs=[[{"type": "bar"}, {"type": "bar"}],
           [{"type": "bar"}, {"type": "scatter"}]]
)

# Chart 1: Base income comparison
fig.add_trace(
    go.Bar(
        x=stall_names,
        y=base_incomes,
        marker=dict(color=colors),
        text=[f'₹{i}/s' for i in base_incomes],
        textposition='outside',
        showlegend=False
    ),
    row=1, col=1
)

# Chart 2: Tap income comparison
fig.add_trace(
    go.Bar(
        x=stall_names,
        y=tap_incomes,
        marker=dict(color=colors),
        text=[f'₹{i}' for i in tap_incomes],
        textposition='outside',
        showlegend=False
    ),
    row=1, col=2
)

# Chart 3: Costs comparison (stacked bar for unlock, upgrade, helper)
fig.add_trace(
    go.Bar(
        x=stall_names,
        y=unlock_costs,
        name='Unlock Cost',
        marker=dict(color='#5D878F'),
        text=[f'₹{int(c)}' for c in unlock_costs],
        textposition='inside'
    ),
    row=2, col=1
)

fig.add_trace(
    go.Bar(
        x=stall_names,
        y=upgrade_costs_l1,
        name='First Upgrade',
        marker=dict(color='#964325'),
        text=[f'₹{int(c)}' for c in upgrade_costs_l1],
        textposition='inside'
    ),
    row=2, col=1
)

fig.add_trace(
    go.Bar(
        x=stall_names,
        y=helper_costs,
        name='First Helper',
        marker=dict(color='#944454'),
        text=[f'₹{int(c)}' for c in helper_costs],
        textposition='inside'
    ),
    row=2, col=1
)

# Chart 4: Efficiency ratio
efficiency = [base_incomes[i] / unlock_costs[i] for i in range(len(stall_names))]

fig.add_trace(
    go.Scatter(
        x=stall_names,
        y=efficiency,
        mode='lines+markers',
        line=dict(color='#2E8B57', width=3),
        marker=dict(size=12, color=colors),
        showlegend=False,
        text=[f'{e:.2f}' for e in efficiency],
        textposition='top center'
    ),
    row=2, col=2
)

# Update layout
fig.update_layout(
    title_text="Street Tycoon: Stall Types Comparison",
    height=900,
    plot_bgcolor='white',
    barmode='stack'
)

# Update axes
fig.update_xaxes(gridcolor='#E5E5E5', row=1, col=1)
fig.update_xaxes(gridcolor='#E5E5E5', row=1, col=2)
fig.update_xaxes(gridcolor='#E5E5E5', row=2, col=1)
fig.update_xaxes(gridcolor='#E5E5E5', row=2, col=2)

fig.update_yaxes(title_text="Income (₹/s)", gridcolor='#E5E5E5', row=1, col=1)
fig.update_yaxes(title_text="Tap Income (₹)", gridcolor='#E5E5E5', row=1, col=2)
fig.update_yaxes(title_text="Cost (₹)", gridcolor='#E5E5E5', row=2, col=1)
fig.update_yaxes(title_text="Efficiency Ratio", gridcolor='#E5E5E5', row=2, col=2)

# Save
fig.write_image('stall_comparison.png', width=1400, height=900)
fig.write_image('stall_comparison.svg', format='svg')

print("✓ Stall comparison chart created")


# Create a detailed progression chart showing income growth for each stall type
fig2 = go.Figure()

levels = np.arange(1, 21)

for stall_name, data in stalls.items():
    base = data["base_income"]
    # Income with 1 helper at each level: baseIncome * 0.5 * (1 + (level - 1) * 0.5)
    incomes = [base * 0.5 * (1 + (level - 1) * 0.5) for level in levels]

    fig2.add_trace(go.Scatter(
        x=levels,
        y=incomes,
        mode='lines+markers',
        name=stall_name,
        line=dict(color=data["color"], width=2),
        marker=dict(size=4)
    ))

fig2.update_layout(
    title="Stall Income Growth with 1 Helper (by Level)",
    xaxis_title="Stall Level",
    yaxis_title="Income per Second (₹/s)",
    plot_bgcolor='white',
    hovermode='x unified'
)

fig2.update_xaxes(gridcolor='#E5E5E5', dtick=2)
fig2.update_yaxes(gridcolor='#E5E5E5')

fig2.write_image('stall_progression_by_type.png', width=1200, height=700)
fig2.write_image('stall_progression_by_type.svg', format='svg')

print("✓ Stall progression by type chart created")
