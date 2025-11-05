import plotly.express as px
import plotly.graph_objects as go
import pandas as pd

# Accurate localization coverage data
# Based on actual implementation: English has full strings, Hindi and Kannada have translations
data = {
    "Language": ["English", "Hindi (हिन्दी)", "Kannada (ಕನ್ನಡ)"],
    "String Count": [20, 20, 20],  # All core strings translated
    "Coverage %": [100, 100, 100]
}

df = pd.DataFrame(data)

# Create bar chart instead of pie chart for clearer information
fig = go.Figure()

fig.add_trace(go.Bar(
    x=df["Language"],
    y=df["String Count"],
    text=df["String Count"],
    textposition='auto',
    marker=dict(
        color=['#1FB8CD', '#DB4545', '#2E8B57'],
        line=dict(color='white', width=2)
    ),
    hovertemplate='<b>%{x}</b><br>Strings: %{y}<br>Coverage: 100%<extra></extra>'
))

fig.update_layout(
    title="Street Tycoon Localization Coverage",
    xaxis_title="Language",
    yaxis_title="Translated Strings",
    plot_bgcolor='white',
    showlegend=False,
    annotations=[
        dict(
            x=0.5, y=1.1,
            xref='paper', yref='paper',
            text='All 20 core UI strings translated across 3 languages',
            showarrow=False,
            font=dict(size=12, color='#666')
        )
    ]
)

# Update y-axis
fig.update_yaxes(gridcolor='#E5E5E5', range=[0, 25])

# Save
fig.write_image('localization_coverage.png', width=1000, height=600)
fig.write_image('localization_coverage.svg', format='svg')

print("✓ Localization coverage chart created")
