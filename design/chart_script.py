
import plotly.graph_objects as go
import json

# Data
data = {"features": ["Map Progression", "Family Spending", "Character System", "UI Enhancement", "Music & Sound"], 
        "completeness": [75, 60, 50, 80, 65], 
        "maturity": [6, 5, 4, 7, 6], 
        "status": ["good", "medium", "critical", "good", "medium"]}

features = data["features"]
completeness = data["completeness"]
maturity = data["maturity"]

# Assign colors based on completeness
colors = []
for comp in completeness:
    if comp >= 70:
        colors.append('#2E8B57')  # Green
    elif comp >= 50:
        colors.append('#D2BA4C')  # Yellow
    else:
        colors.append('#DB4545')  # Red

# Create abbreviated labels for the features (max 15 chars)
abbreviated_features = []
for feat in features:
    if len(feat) <= 15:
        abbreviated_features.append(feat)
    else:
        # Abbreviate longer names
        abbreviated_features.append(feat[:12] + "...")

# Create text labels with feature name and percentage
text_labels = [f"{abbreviated_features[i]}<br>{completeness[i]}%" for i in range(len(features))]

# Create figure
fig = go.Figure()

# Add scatter plot with circles
fig.add_trace(go.Scatter(
    x=maturity,
    y=completeness,
    mode='markers+text',
    marker=dict(
        size=40,
        color=colors,
        line=dict(width=2, color='white')
    ),
    text=text_labels,
    textposition="middle center",
    textfont=dict(size=9, color='white'),
    hovertemplate='<b>%{text}</b><br>Maturity: %{x}<br>Complete: %{y}%<extra></extra>',
    showlegend=False
))

# Add horizontal reference lines at 70% and 80%
fig.add_hline(y=70, line_dash="dot", line_color="gray", opacity=0.5)
fig.add_hline(y=80, line_dash="dot", line_color="gray", opacity=0.5)

# Update layout
fig.update_layout(
    title="Feature Gap Analysis",
    xaxis_title="Maturity (0-10)",
    yaxis_title="Complete %",
)

# Update axes
fig.update_xaxes(range=[0, 10], dtick=1)
fig.update_yaxes(range=[0, 100], dtick=10)

# Clip on axis false
fig.update_traces(cliponaxis=False)

# Save as PNG and SVG
fig.write_image("feature_gap_analysis.png")
fig.write_image("feature_gap_analysis.svg", format="svg")
