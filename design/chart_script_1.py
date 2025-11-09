
import plotly.graph_objects as go
import json

# Data provided
data = {
    "phases": [
        {"name": "Tutorial System", "start": 1, "duration": 2, "color": "#3b82f6", "milestone": "Tutorial Complete"},
        {"name": "Analytics & Crash", "start": 3, "duration": 1, "color": "#ef4444", "milestone": "Analytics Live"},
        {"name": "Save Backup System", "start": 4, "duration": 1, "color": "#f97316", "milestone": "Recovery Ops"},
        {"name": "Settings & Testing", "start": 5, "duration": 2, "color": "#22c55e", "milestone": "Launch Ready"}
    ],
    "totalWeeks": 6
}

# Create figure
fig = go.Figure()

# Add bars for each phase
for phase in data['phases']:
    fig.add_trace(go.Bar(
        name=phase['name'],
        x=[phase['duration']],
        y=[phase['name']],
        orientation='h',
        marker=dict(color=phase['color']),
        base=[phase['start'] - 1],  # Start position (0-indexed for weeks)
        text=[phase['milestone']],
        textposition='inside',
        textfont=dict(color='white', size=12),
        hovertemplate=f"<b>{phase['name']}</b><br>Week {phase['start']}-{phase['start'] + phase['duration'] - 1}<br>Milestone: {phase['milestone']}<extra></extra>",
        showlegend=False
    ))

# Update layout
fig.update_layout(
    title='Street-Tycoon 6-Week Roadmap',
    xaxis_title='Week',
    yaxis_title='Phase',
    xaxis=dict(
        tickmode='linear',
        tick0=1,
        dtick=1,
        range=[0, 7],
        tickvals=[1, 2, 3, 4, 5, 6],
        ticktext=['Week 1', 'Week 2', 'Week 3', 'Week 4', 'Week 5', 'Week 6']
    ),
    barmode='overlay',
    showlegend=False
)

fig.update_yaxes(autorange='reversed')
fig.update_traces(cliponaxis=False)

# Save the chart
fig.write_image('roadmap.png')
fig.write_image('roadmap.svg', format='svg')
