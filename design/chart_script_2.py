import plotly.express as px
import pandas as pd

# Create the data
data = {
    "Language": ["English", "Hindi", "Kannada"], 
    "CoveragePercent": [50, 25, 25]
}

df = pd.DataFrame(data)

# Create pie chart
fig = px.pie(df, 
             values='CoveragePercent',
             names='Language',
             title='Game Localization Coverage by Language')

# Apply pie chart specific formatting
fig.update_layout(uniformtext_minsize=14, uniformtext_mode='hide')

# Save as both PNG and SVG
fig.write_image('localization_coverage.png')
fig.write_image('localization_coverage.svg', format='svg')

fig.show()