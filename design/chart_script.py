
import plotly.graph_objects as go
import json

# Data
data = {
  "issue_distribution": [
    {"Priority": "Critical", "Count": 4, "Color": "#DC2626"},
    {"Priority": "High", "Count": 6, "Color": "#EA580C"},
    {"Priority": "Medium", "Count": 7, "Color": "#CA8A04"},
    {"Priority": "Low", "Count": 5, "Color": "#16A34A"}
  ]
}

# Extract data for the chart
priorities = [item["Priority"] for item in data["issue_distribution"]]
counts = [item["Count"] for item in data["issue_distribution"]]
colors = [item["Color"] for item in data["issue_distribution"]]

# Reverse the order so Critical is at the top
priorities.reverse()
counts.reverse()
colors.reverse()

# Create horizontal bar chart
fig = go.Figure()

fig.add_trace(go.Bar(
    y=priorities,
    x=counts,
    orientation='h',
    marker=dict(color=colors),
    text=counts,
    textposition='outside',
    hovertemplate='%{y}: %{x} issues<extra></extra>'
))

fig.update_layout(
    title="Issue Distribution by Priority",
    xaxis_title="Num of Issues",
    yaxis_title="Priority Level",
    showlegend=False
)

fig.update_traces(cliponaxis=False)

# Save as PNG and SVG
fig.write_image("chart.png")
fig.write_image("chart.svg", format="svg")
