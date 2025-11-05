import plotly.express as px
import pandas as pd

# Create the data
data = {
    "Monetization Model": ["Rewarded Ads", "Cosmetic IAPs", "Pay-to-Progress IAPs", "Subscriptions", "Banner/Interstitial Ads"], 
    "Number of Games": [28, 23, 17, 4, 18]
}

df = pd.DataFrame(data)

# Abbreviate the monetization model names to fit 15 char limit
df["Model_Short"] = ["Rewarded Ads", "Cosmetic IAPs", "Pay-to-Prog", "Subscriptions", "Banner/Inter"]

# Create the bar chart
fig = px.bar(df, 
             x="Model_Short", 
             y="Number of Games",
             title="Android Tycoon Monetization Models",
             color_discrete_sequence=["#1FB8CD", "#DB4545", "#2E8B57", "#5D878F", "#D2BA4C"])

# Update traces for better appearance
fig.update_traces(cliponaxis=False)

# Update layout
fig.update_layout(
    xaxis_title="Monetization",
    yaxis_title="Number of Games"
)

# Update x-axis to rotate labels if needed for better readability
fig.update_xaxes(tickangle=45)

# Save as both PNG and SVG
fig.write_image("monetization_chart.png")
fig.write_image("monetization_chart.svg", format="svg")

fig.show()