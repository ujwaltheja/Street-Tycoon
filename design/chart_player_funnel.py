import plotly.graph_objects as go
from plotly.subplots import make_subplots
import numpy as np

# Player journey and retention funnel
# Based on typical incremental game metrics

# Retention funnel (estimated targets from README)
funnel_stages = [
    "Install",
    "Complete Tutorial",
    "Play 5 Minutes",
    "Unlock 2nd Stall",
    "Day 1 Return",
    "Day 7 Return",
    "First IAP"
]

# Target percentages
funnel_values = [100, 80, 70, 60, 35, 10, 2]
colors_funnel = ['#1FB8CD', '#2E8B57', '#D2BA4C', '#DB4545', '#964325', '#944454', '#5D878F']

# Create subplots
fig = make_subplots(
    rows=2, cols=2,
    subplot_titles=(
        'Player Retention Funnel',
        'Session Length Distribution (Target)',
        'Player Progression Timeline',
        'Engagement Metrics Targets'
    ),
    specs=[[{"type": "funnel"}, {"type": "box"}],
           [{"type": "scatter"}, {"type": "bar"}]]
)

# Chart 1: Retention funnel
fig.add_trace(
    go.Funnel(
        y=funnel_stages,
        x=funnel_values,
        textinfo="value+percent previous",
        marker=dict(color=colors_funnel),
        connector=dict(line=dict(color="lightgray", width=2))
    ),
    row=1, col=1
)

# Chart 2: Session length distribution (box plot simulation)
# Target: 1-5 minute sessions with meaningful progress
session_types = ['Session 1', 'Session 2-5', 'Session 6-10', 'Session 10+']
session_lengths = [
    [1, 2, 3, 4, 5],  # First sessions (tutorial)
    [2, 3, 4, 5, 6, 3, 4],  # Early exploration
    [3, 4, 5, 6, 7, 5, 6],  # Mid game
    [4, 5, 6, 7, 8, 9, 5, 6, 7]  # Late game (longer sessions)
]

for i, (session_type, lengths) in enumerate(zip(session_types, session_lengths)):
    fig.add_trace(
        go.Box(
            y=lengths,
            name=session_type,
            marker_color=colors_funnel[i],
            boxmean='sd'
        ),
        row=1, col=2
    )

# Chart 3: Player progression timeline (milestones)
milestones = {
    0: "Start Game",
    2: "Unlock 1st Helper",
    5: "Unlock 2nd Stall",
    15: "Unlock 2nd Zone",
    30: "Unlock 3rd Zone",
    60: "Reach 10 Helpers",
    120: "Unlock 4th Zone",
    180: "Late Game"
}

milestone_times = list(milestones.keys())
milestone_names = list(milestones.values())
milestone_cash = [100, 500, 1000, 5000, 15000, 50000, 100000, 500000]

fig.add_trace(
    go.Scatter(
        x=milestone_times,
        y=milestone_cash,
        mode='lines+markers',
        line=dict(color='#2E8B57', width=3),
        marker=dict(size=10, color=colors_funnel[:len(milestone_times)]),
        text=milestone_names,
        textposition='top center',
        showlegend=False
    ),
    row=2, col=1
)

# Chart 4: Key engagement metrics targets
metrics = ['D1 Retention', 'D7 Retention', 'Avg Session', 'Sessions/Day', 'Ad Completion']
target_values = [35, 10, 3.5, 4, 75]  # D1: 35%, D7: 10%, Session: 3.5 min, etc.

fig.add_trace(
    go.Bar(
        x=metrics,
        y=target_values,
        marker=dict(color=colors_funnel[:5]),
        text=[f'{v}%' if i < 2 or i == 4 else f'{v}' for i, v in enumerate(target_values)],
        textposition='outside',
        showlegend=False
    ),
    row=2, col=2
)

# Update layout
fig.update_layout(
    title_text="Street Tycoon: Player Journey & Retention Analysis",
    height=1000,
    showlegend=True,
    plot_bgcolor='white'
)

# Update axes
fig.update_xaxes(title_text="", row=1, col=1)
fig.update_xaxes(title_text="Session Group", row=1, col=2)
fig.update_xaxes(title_text="Time (minutes played)", gridcolor='#E5E5E5', row=2, col=1)
fig.update_xaxes(title_text="Metric", tickangle=45, row=2, col=2)

fig.update_yaxes(title_text="Players (%)", row=1, col=1)
fig.update_yaxes(title_text="Session Length (minutes)", gridcolor='#E5E5E5', row=1, col=2)
fig.update_yaxes(title_text="Cash (₹)", type='log', gridcolor='#E5E5E5', row=2, col=1)
fig.update_yaxes(title_text="Value", gridcolor='#E5E5E5', row=2, col=2)

# Save
fig.write_image('player_funnel.png', width=1400, height=1000)
fig.write_image('player_funnel.svg', format='svg')

print("✓ Player funnel and journey chart created")
