import plotly.graph_objects as go
import plotly.express as px

# Street Tycoon specific monetization flow
fig = go.Figure()

# Define monetization components and their relationships
components = {
    # Entry points
    "Player": {"pos": (1, 8), "color": "#1FB8CD", "size": 100},

    # Free gameplay loop
    "Free Gameplay": {"pos": (1, 6.5), "color": "#2E8B57", "size": 90},

    # Monetization triggers
    "Need Boost": {"pos": (0.3, 5), "color": "#D2BA4C", "size": 70},
    "Want Tokens": {"pos": (1, 5), "color": "#D2BA4C", "size": 70},
    "Like Game": {"pos": (1.7, 5), "color": "#D2BA4C", "size": 70},

    # Monetization types
    "Rewarded Ad": {"pos": (0.3, 3.5), "color": "#DB4545", "size": 80},
    "IAP Tokens": {"pos": (1, 3.5), "color": "#DB4545", "size": 80},
    "Cosmetics": {"pos": (1.7, 3.5), "color": "#DB4545", "size": 80},

    # Rewards
    "2x Earnings": {"pos": (0, 2), "color": "#964325", "size": 60},
    "Instant Upgrade": {"pos": (0.6, 2), "color": "#964325", "size": 60},
    "Tokens": {"pos": (1, 2), "color": "#964325", "size": 60},
    "Stall Skins": {"pos": (1.7, 2), "color": "#964325", "size": 60},

    # Outcome
    "Enhanced Experience": {"pos": (1, 0.5), "color": "#2E8B57", "size": 100},
}

# Define flow connections
connections = [
    ("Player", "Free Gameplay", "Play"),
    ("Free Gameplay", "Need Boost", "Want progress"),
    ("Free Gameplay", "Want Tokens", "Saw cosmetic"),
    ("Free Gameplay", "Like Game", "Enjoy game"),

    ("Need Boost", "Rewarded Ad", "Watch ad"),
    ("Want Tokens", "IAP Tokens", "Purchase"),
    ("Like Game", "IAP Tokens", "Support dev"),
    ("Like Game", "Cosmetics", "Express self"),

    ("Rewarded Ad", "2x Earnings", ""),
    ("Rewarded Ad", "Instant Upgrade", ""),
    ("IAP Tokens", "Tokens", ""),
    ("Tokens", "Stall Skins", "Spend"),
    ("Cosmetics", "Stall Skins", ""),

    ("2x Earnings", "Enhanced Experience", ""),
    ("Instant Upgrade", "Enhanced Experience", ""),
    ("Stall Skins", "Enhanced Experience", ""),
]

# Draw connections
for source, target, label in connections:
    if source in components and target in components:
        x0, y0 = components[source]["pos"]
        x1, y1 = components[target]["pos"]

        # Calculate arrow
        fig.add_annotation(
            x=x1, y=y1,
            ax=x0, ay=y0,
            xref='x', yref='y',
            axref='x', ayref='y',
            showarrow=True,
            arrowhead=2,
            arrowsize=1,
            arrowwidth=2,
            arrowcolor='#999999'
        )

        if label:
            mid_x, mid_y = (x0 + x1) / 2, (y0 + y1) / 2
            fig.add_annotation(
                x=mid_x, y=mid_y,
                text=label,
                showarrow=False,
                font=dict(size=8, color='#666'),
                bgcolor='white'
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

# Add labels for sections
fig.add_annotation(x=0.1, y=6.5, text="Core Loop", showarrow=False,
                   font=dict(size=12, color='#2E8B57'), xanchor='left')
fig.add_annotation(x=0.1, y=5, text="Triggers", showarrow=False,
                   font=dict(size=12, color='#D2BA4C'), xanchor='left')
fig.add_annotation(x=0.1, y=3.5, text="Monetization", showarrow=False,
                   font=dict(size=12, color='#DB4545'), xanchor='left')
fig.add_annotation(x=0.1, y=2, text="Rewards", showarrow=False,
                   font=dict(size=12, color='#964325'), xanchor='left')

# Add monetization mix pie chart as inset
fig.add_trace(go.Pie(
    labels=['Rewarded Ads', 'IAP Tokens', 'Cosmetics'],
    values=[60, 30, 10],
    hole=0.4,
    domain=dict(x=[0.75, 0.95], y=[0.75, 0.95]),
    marker=dict(colors=['#DB4545', '#964325', '#2E8B57']),
    showlegend=False,
    textinfo='label+percent',
    textfont=dict(size=8)
))

fig.update_layout(
    title="Street Tycoon: Monetization Flow",
    showlegend=False,
    xaxis=dict(showgrid=False, zeroline=False, showticklabels=False, range=[-0.3, 2.2]),
    yaxis=dict(showgrid=False, zeroline=False, showticklabels=False, range=[0, 8.5]),
    plot_bgcolor='white',
    width=1200,
    height=1000,
    annotations=list(fig.layout.annotations) + [
        dict(
            x=0.85, y=0.92,
            xref='paper', yref='paper',
            text='<b>Revenue Mix</b>',
            showarrow=False,
            font=dict(size=10)
        ),
        dict(
            x=0.5, y=0.05,
            xref='paper', yref='paper',
            text='<b>Key Principles:</b> Non-intrusive | Value-driven | Player choice | Fair pricing',
            showarrow=False,
            font=dict(size=11, color='#333'),
            bgcolor='rgba(255,255,255,0.9)',
            bordercolor='#CCCCCC',
            borderwidth=1
        )
    ]
)

fig.write_image('monetization_flow.png', width=1200, height=1000)
fig.write_image('monetization_flow.svg', format='svg')

print("✓ Monetization flow diagram created")
