import plotly.graph_objects as go
import plotly.express as px
import numpy as np

# Define the nodes for the Street Tycoon architecture
nodes = [
    "Jetpack Compose UI",
    "GameViewModel", 
    "GameRepository\n(Room db)",
    "GameSimulation\n(Kotlin wrapper)",
    "JNI Bridge",
    "Native C++ Game\nSimulation\n(authoritative state)",
    "JSON State\nManagement"
]

# Define positions for nodes in a logical flow layout
node_positions = {
    0: (0.2, 0.8),    # Jetpack Compose UI
    1: (0.2, 0.5),    # GameViewModel
    2: (0.05, 0.2),   # GameRepository
    3: (0.4, 0.5),    # GameSimulation
    4: (0.6, 0.5),    # JNI Bridge
    5: (0.8, 0.5),    # Native C++ Game Simulation
    6: (0.8, 0.2),    # JSON State Management
}

# Define connections (edges) - using node indices
connections = [
    (0, 1, "User Actions / UI State"),     # UI <-> ViewModel
    (1, 2, "Persist / Load Data"),         # ViewModel <-> Repository
    (1, 3, "Game Commands / State"),       # ViewModel <-> GameSimulation
    (3, 4, "Native Calls / Updates"),      # GameSimulation <-> JNI
    (4, 5, "State Updates"),               # JNI <-> C++
    (5, 6, "Serialize / Deserialize"),     # C++ <-> JSON
    (2, 6, "Data Storage"),                # Repository <-> JSON
]

# Create the network diagram
fig = go.Figure()

# Add edges first (so they appear behind nodes)
for start, end, label in connections:
    x0, y0 = node_positions[start]
    x1, y1 = node_positions[end]
    
    # Add bidirectional arrows as lines
    fig.add_trace(go.Scatter(
        x=[x0, x1], y=[y0, y1],
        mode='lines',
        line=dict(color='#5D878F', width=2),
        showlegend=False,
        hoverinfo='skip'
    ))
    
    # Add arrow heads
    mid_x, mid_y = (x0 + x1) / 2, (y0 + y1) / 2
    
    # Calculate arrow direction
    dx, dy = x1 - x0, y1 - y0
    length = np.sqrt(dx**2 + dy**2)
    if length > 0:
        # Normalize and scale for arrow
        dx_norm, dy_norm = dx/length * 0.02, dy/length * 0.02
        
        # Add arrow head
        fig.add_trace(go.Scatter(
            x=[mid_x - dx_norm, mid_x, mid_x - dx_norm],
            y=[mid_y - dy_norm, mid_y, mid_y + dy_norm],
            mode='lines',
            line=dict(color='#5D878F', width=2),
            showlegend=False,
            hoverinfo='skip'
        ))

# Add nodes
colors = ['#1FB8CD', '#DB4545', '#2E8B57', '#D2BA4C', '#B4413C', '#964325', '#944454']

for i, (node, pos) in enumerate(zip(nodes, node_positions.values())):
    fig.add_trace(go.Scatter(
        x=[pos[0]], y=[pos[1]],
        mode='markers+text',
        marker=dict(
            size=80,
            color=colors[i % len(colors)],
            line=dict(color='white', width=2)
        ),
        text=node,
        textposition='middle center',
        textfont=dict(color='white', size=10, family='Arial Black'),
        showlegend=False,
        hovertemplate=f'<b>{node}</b><extra></extra>'
    ))

# Update layout
fig.update_layout(
    title="Street Tycoon Game MVP Architecture",
    showlegend=False,
    xaxis=dict(showgrid=False, zeroline=False, showticklabels=False),
    yaxis=dict(showgrid=False, zeroline=False, showticklabels=False),
    plot_bgcolor='white',
    annotations=[
        dict(
            x=0.1, y=0.9,
            text="User Layer",
            showarrow=False,
            font=dict(size=12, color='#13343B')
        ),
        dict(
            x=0.1, y=0.35,
            text="Data Layer", 
            showarrow=False,
            font=dict(size=12, color='#13343B')
        ),
        dict(
            x=0.5, y=0.9,
            text="Business Logic",
            showarrow=False,
            font=dict(size=12, color='#13343B')
        ),
        dict(
            x=0.8, y=0.9,
            text="Native Layer",
            showarrow=False,
            font=dict(size=12, color='#13343B')
        )
    ]
)

# Save the chart
fig.write_image("street_tycoon_architecture.png")
fig.write_image("street_tycoon_architecture.svg", format="svg")

print("Architecture diagram created successfully!")