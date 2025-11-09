
import plotly.graph_objects as go
import pandas as pd

# Data
data = {
    "systems": ["Tutorial & Onboarding", "Analytics & Crash Reporting", "Save Backup System", "Settings Persistence"],
    "current_status": [0, 0, 30, 20],
    "target_status": [100, 100, 100, 100],
    "effort_weeks": [2, 1, 1, 0.5],
    "risk_level": ["CRITICAL", "CRITICAL", "CRITICAL", "HIGH"]
}

# Shorten system names for chart (15 char limit)
short_names = ["Tutorial", "Analytics", "Save System", "Settings"]

# Color coding based on current status per instructions
def get_status_color(status):
    if status <= 30:
        return '#DB4545'  # Red for 0-30% (critical)
    elif status <= 60:
        return '#D2BA4C'  # Yellow for 30-60% (warning)
    else:
        return '#2E8B57'  # Green for 60-100% (okay)

# Create colors for each system based on current status
current_colors = [get_status_color(status) for status in data['current_status']]

# Create figure
fig = go.Figure()

# Add Target Status bars (light background to show goal)
fig.add_trace(go.Bar(
    y=short_names,
    x=data['target_status'],
    name='Target %',
    orientation='h',
    marker=dict(color='#E0E0E0', line=dict(color='#999999', width=1)),
    cliponaxis=False,
    hovertemplate='%{y}<br>Target: %{x}%<extra></extra>'
))

# Add Current Status bars (colored by status level)
fig.add_trace(go.Bar(
    y=short_names,
    x=data['current_status'],
    name='Current %',
    orientation='h',
    marker=dict(color=current_colors),
    text=[f"{val}%" if val > 0 else "0%" for val in data['current_status']],
    textposition='outside',
    cliponaxis=False,
    hovertemplate='%{y}<br>Current: %{x}%<extra></extra>'
))

# Add Effort markers (as scatter points)
effort_x = [110] * len(data['effort_weeks'])  # Position to the right of bars

fig.add_trace(go.Scatter(
    y=short_names,
    x=effort_x,
    mode='markers+text',
    name='Effort (weeks)',
    marker=dict(size=15, color='#1FB8CD', symbol='diamond'),
    text=[f"{e}w" for e in data['effort_weeks']],
    textposition='middle right',
    cliponaxis=False,
    customdata=data['effort_weeks'],
    hovertemplate='%{y}<br>Effort: %{customdata} wks<extra></extra>'
))

# Update layout
fig.update_layout(
    title='Priority Matrix: Critical Systems',
    xaxis_title='Completion %',
    yaxis_title='System',
    barmode='overlay',  # Overlay to show current on top of target
    legend=dict(orientation='h', yanchor='bottom', y=1.05, xanchor='center', x=0.5),
    xaxis=dict(range=[0, 130]),  # Extended range for effort markers
    yaxis=dict(autorange='reversed')  # Show top priority first
)

fig.update_traces(cliponaxis=False)

# Save as PNG and SVG
fig.write_image('priority_matrix.png')
fig.write_image('priority_matrix.svg', format='svg')
