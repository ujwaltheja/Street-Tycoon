
import plotly.graph_objects as go
import json

# Load the data
data = {
  "performance_metrics": [
    {"Metric": "Frame Rate (FPS)", "Before": 40, "After": 58, "Target": 60, "Improvement": "45%"},
    {"Metric": "Recompositions/sec", "Before": 50, "After": 25, "Target": 30, "Improvement": "50%"},
    {"Metric": "Memory (MB)", "Before": 220, "After": 180, "Target": 200, "Improvement": "18%"},
    {"Metric": "Battery %/hour", "Before": 8.0, "After": 5.5, "Target": 5.0, "Improvement": "31%"},
    {"Metric": "JNI Calls/sec", "Before": 20, "After": 8, "Target": 10, "Improvement": "60%"}
  ]
}

# Extract data
metrics = [item["Metric"] for item in data["performance_metrics"]]
before_values = [item["Before"] for item in data["performance_metrics"]]
after_values = [item["After"] for item in data["performance_metrics"]]
improvements = [item["Improvement"] for item in data["performance_metrics"]]

# Shorten metric names to meet 15 char limit
short_metrics = [
    "Frame Rate",
    "Recomp/sec",
    "Memory (MB)",
    "Battery %/hr",
    "JNI Calls/sec"
]

# Create figure
fig = go.Figure()

# Add Before Fixes bars (orange/gray)
fig.add_trace(go.Bar(
    name='Before Fixes',
    x=short_metrics,
    y=before_values,
    marker_color='#D2BA4C',  # Using orange-ish color from brand colors
    text=before_values,
    textposition='none',
    cliponaxis=False
))

# Add After Fixes bars (green)
fig.add_trace(go.Bar(
    name='After Fixes',
    x=short_metrics,
    y=after_values,
    marker_color='#2E8B57',  # Using sea green from brand colors
    text=improvements,
    textposition='outside',
    textfont=dict(size=12),
    cliponaxis=False
))

# Update layout
fig.update_layout(
    title='Performance Improvements',
    xaxis_title='Metrics',
    yaxis_title='Value',
    barmode='group',
    legend=dict(
        orientation='h',
        yanchor='bottom',
        y=1.05,
        xanchor='center',
        x=0.5
    )
)

# Save as PNG and SVG
fig.write_image('performance_comparison.png')
fig.write_image('performance_comparison.svg', format='svg')
