import plotly.graph_objects as go
import plotly.express as px

# Create a flowchart-style chart using Plotly with shapes and annotations
fig = go.Figure()

# Define positions for nodes
nodes = {
    'Map 1 Locked': (2, 8),
    '10 Upgrades': (0.5, 6),
    '5 Helpers': (1.5, 6),
    '₹5k Earned': (2.5, 6),
    '24hrs Play': (3.5, 6),
    'Gates Complete': (2, 4),
    'Map 2 Unlocks': (2, 2),
    'Real-Life Spend': (6, 8),
    'Housing': (5, 6),
    'Transport': (6, 6),
    'Family': (7, 6),
    'Education': (5.5, 5),
    'Health': (6.5, 5)
}

# Colors for different node types
colors = {
    'Map 1 Locked': '#FFCDD2',
    '10 Upgrades': '#B3E5EC',
    '5 Helpers': '#B3E5EC', 
    '₹5k Earned': '#B3E5EC',
    '24hrs Play': '#B3E5EC',
    'Gates Complete': '#FFEB8A',
    'Map 2 Unlocks': '#A5D6A7',
    'Real-Life Spend': '#9FA8B0',
    'Housing': '#B3E5EC',
    'Transport': '#B3E5EC',
    'Family': '#B3E5EC',
    'Education': '#B3E5EC',
    'Health': '#B3E5EC'
}

# Add rectangles for each node
for node, (x, y) in nodes.items():
    fig.add_shape(
        type="rect",
        x0=x-0.4, y0=y-0.3, x1=x+0.4, y1=y+0.3,
        fillcolor=colors[node],
        line=dict(color="black", width=1)
    )
    
    fig.add_annotation(
        x=x, y=y,
        text=node,
        showarrow=False,
        font=dict(size=10, color="black"),
        align="center"
    )

# Add arrows/connections
connections = [
    ('Map 1 Locked', '10 Upgrades'),
    ('Map 1 Locked', '5 Helpers'),
    ('Map 1 Locked', '₹5k Earned'),
    ('Map 1 Locked', '24hrs Play'),
    ('10 Upgrades', 'Gates Complete'),
    ('5 Helpers', 'Gates Complete'),
    ('₹5k Earned', 'Gates Complete'),
    ('24hrs Play', 'Gates Complete'),
    ('Gates Complete', 'Map 2 Unlocks'),
    ('Map 1 Locked', 'Real-Life Spend'),
    ('Real-Life Spend', 'Housing'),
    ('Real-Life Spend', 'Transport'),
    ('Real-Life Spend', 'Family'),
    ('Real-Life Spend', 'Education'),
    ('Real-Life Spend', 'Health')
]

# Add arrows
for start, end in connections:
    x0, y0 = nodes[start]
    x1, y1 = nodes[end]
    
    fig.add_annotation(
        x=x1, y=y1,
        ax=x0, ay=y0,
        xref="x", yref="y",
        axref="x", ayref="y",
        arrowhead=2,
        arrowsize=1,
        arrowwidth=2,
        arrowcolor="black",
        showarrow=True,
        text=""
    )

# Update layout
fig.update_layout(
    title="Street Tycoon Map Unlock System",
    showlegend=False,
    xaxis=dict(showgrid=False, zeroline=False, showticklabels=False, range=[-1, 8]),
    yaxis=dict(showgrid=False, zeroline=False, showticklabels=False, range=[1, 9]),
    plot_bgcolor="white",
    annotations=[ann for ann in fig.layout.annotations]
)

# Save the chart
fig.write_image("street_tycoon_flowchart.png")
fig.write_image("street_tycoon_flowchart.svg", format="svg")
print("Chart saved as PNG and SVG")