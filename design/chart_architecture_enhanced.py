import plotly.graph_objects as go
import plotly.express as px

# Enhanced architecture with timing and flow details
fig = go.Figure()

# Define layers and components with precise positioning
components = {
    # UI Layer
    "Compose UI": {"pos": (1, 9), "color": "#1FB8CD", "size": 100},
    "MapScreen": {"pos": (0.5, 8), "color": "#1FB8CD", "size": 60},
    "StallScreen": {"pos": (1, 8), "color": "#1FB8CD", "size": 60},
    "ShopScreen": {"pos": (1.5, 8), "color": "#1FB8CD", "size": 60},

    # ViewModel Layer
    "GameViewModel": {"pos": (1, 6.5), "color": "#DB4545", "size": 100},
    "Tick Loop\n(100ms)": {"pos": (0.3, 5.5), "color": "#D2BA4C", "size": 70},
    "Auto-save\n(30s)": {"pos": (1.7, 5.5), "color": "#D2BA4C", "size": 70},

    # Data Layer
    "GameRepository": {"pos": (1.7, 4), "color": "#2E8B57", "size": 80},
    "Room DB": {"pos": (1.7, 2.5), "color": "#2E8B57", "size": 70},

    # Simulation Layer
    "GameSimulation\n(Kotlin)": {"pos": (1, 4), "color": "#964325", "size": 90},
    "JNI Bridge": {"pos": (1, 2.5), "color": "#5D878F", "size": 80},
    "C++ GameSim": {"pos": (1, 1), "color": "#944454", "size": 90},
}

# Define connections with labels
connections = [
    # UI to ViewModel
    ("Compose UI", "GameViewModel", "Actions & State Flow"),
    ("MapScreen", "GameViewModel", ""),
    ("StallScreen", "GameViewModel", ""),
    ("ShopScreen", "GameViewModel", ""),

    # ViewModel to loops
    ("GameViewModel", "Tick Loop\n(100ms)", ""),
    ("GameViewModel", "Auto-save\n(30s)", ""),

    # ViewModel to data
    ("GameViewModel", "GameRepository", "Save/Load"),
    ("GameRepository", "Room DB", "Persist JSON"),

    # ViewModel to simulation
    ("GameViewModel", "GameSimulation\n(Kotlin)", "Commands"),
    ("Tick Loop\n(100ms)", "GameSimulation\n(Kotlin)", "tick(deltaMs)"),

    # Simulation chain
    ("GameSimulation\n(Kotlin)", "JNI Bridge", "Native Calls"),
    ("JNI Bridge", "C++ GameSim", "State Updates"),

    # Save path
    ("GameSimulation\n(Kotlin)", "GameRepository", "getSnapshot()"),
]

# Draw connections first (so they're behind nodes)
for source, target, label in connections:
    if source in components and target in components:
        x0, y0 = components[source]["pos"]
        x1, y1 = components[target]["pos"]

        fig.add_trace(go.Scatter(
            x=[x0, x1],
            y=[y0, y1],
            mode='lines',
            line=dict(color='#CCCCCC', width=2),
            showlegend=False,
            hoverinfo='skip'
        ))

        # Add arrow head
        if label:
            mid_x, mid_y = (x0 + x1) / 2, (y0 + y1) / 2
            fig.add_annotation(
                x=mid_x, y=mid_y,
                text=label,
                showarrow=False,
                font=dict(size=8, color='#666'),
                bgcolor='white',
                borderpad=2
            )

# Draw nodes
for name, data in components.items():
    x, y = data["pos"]
    color = data["color"]
    size = data["size"]

    fig.add_trace(go.Scatter(
        x=[x],
        y=[y],
        mode='markers+text',
        marker=dict(
            size=size,
            color=color,
            line=dict(color='white', width=2)
        ),
        text=name,
        textposition='middle center',
        textfont=dict(color='white', size=9, family='Arial'),
        showlegend=False,
        hovertemplate=f'<b>{name}</b><extra></extra>'
    ))

# Add layer labels
layers = [
    {"x": 0.1, "y": 9, "text": "UI Layer", "color": "#1FB8CD"},
    {"x": 0.1, "y": 6.5, "text": "ViewModel", "color": "#DB4545"},
    {"x": 0.1, "y": 4, "text": "Data & Logic", "color": "#2E8B57"},
    {"x": 0.1, "y": 1, "text": "Native Core", "color": "#944454"},
]

for layer in layers:
    fig.add_annotation(
        x=layer["x"], y=layer["y"],
        text=layer["text"],
        showarrow=False,
        font=dict(size=14, color=layer["color"], family='Arial Black'),
        xanchor='left'
    )

# Add timing annotations
fig.add_annotation(
    x=1, y=0,
    text="<b>Key Timings:</b><br>• Tick Loop: 100ms intervals<br>• Auto-save: 30 second intervals<br>• JNI calls: ~1-2ms overhead<br>• UI updates: React to StateFlow",
    showarrow=False,
    xref='paper', yref='paper',
    xanchor='center',
    yanchor='bottom',
    font=dict(size=10, color='#333'),
    bgcolor='rgba(255,255,255,0.9)',
    bordercolor='#CCCCCC',
    borderwidth=1,
    borderpad=8
)

# Update layout
fig.update_layout(
    title="Street Tycoon: Enhanced Architecture with Timing Details",
    showlegend=False,
    xaxis=dict(showgrid=False, zeroline=False, showticklabels=False, range=[-0.2, 2.2]),
    yaxis=dict(showgrid=False, zeroline=False, showticklabels=False, range=[-0.5, 10]),
    plot_bgcolor='white',
    width=1200,
    height=1000
)

# Save
fig.write_image('architecture_enhanced.png', width=1200, height=1000)
fig.write_image('architecture_enhanced.svg', format='svg')

print("✓ Enhanced architecture diagram created")
