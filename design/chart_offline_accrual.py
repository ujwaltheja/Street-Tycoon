import plotly.graph_objects as go
import numpy as np

# Offline accrual formula from game_simulation.cpp
# - Maximum offline time: 4 hours (14400 seconds)
# - Efficiency penalty: 70% of online earnings

def calculate_offline_earnings(income_per_sec, offline_hours):
    """
    Calculate offline earnings with cap and penalty
    Max offline time: 4 hours
    Efficiency: 70%
    """
    MAX_HOURS = 4
    EFFICIENCY = 0.7

    capped_hours = min(offline_hours, MAX_HOURS)
    offline_seconds = capped_hours * 3600
    earnings = income_per_sec * offline_seconds * EFFICIENCY

    return earnings, capped_hours

# Simulate different income rates
income_rates = {
    "Early Game (₹1/s)": 1.0,
    "Mid Game (₹10/s)": 10.0,
    "Late Game (₹50/s)": 50.0,
    "End Game (₹200/s)": 200.0
}

offline_hours = np.linspace(0, 8, 100)  # 0 to 8 hours

# Create figure
fig = go.Figure()

colors = ['#1FB8CD', '#2E8B57', '#D2BA4C', '#DB4545']

for i, (label, rate) in enumerate(income_rates.items()):
    earnings = []
    for hours in offline_hours:
        earned, _ = calculate_offline_earnings(rate, hours)
        earnings.append(earned)

    fig.add_trace(go.Scatter(
        x=offline_hours,
        y=earnings,
        mode='lines',
        name=label,
        line=dict(color=colors[i], width=3),
        hovertemplate=f'<b>{label}</b><br>Time: %{{x:.1f}}h<br>Earnings: ₹%{{y:.0f}}<extra></extra>'
    ))

# Add vertical line at 4 hours (cap)
fig.add_vline(
    x=4,
    line_dash="dash",
    line_color="red",
    annotation_text="4 Hour Cap",
    annotation_position="top"
)

# Add shaded region showing penalty zone
fig.add_vrect(
    x0=0, x1=4,
    fillcolor="green", opacity=0.1,
    annotation_text="70% Efficiency", annotation_position="top left"
)

fig.add_vrect(
    x0=4, x1=8,
    fillcolor="red", opacity=0.05,
    annotation_text="Capped (no additional earnings)", annotation_position="top right"
)

# Update layout
fig.update_layout(
    title="Offline Earnings Calculation<br><sub>Formula: min(offlineTime, 4h) × incomePerSec × 0.7</sub>",
    xaxis_title="Offline Time (hours)",
    yaxis_title="Earnings (₹)",
    plot_bgcolor='white',
    hovermode='x unified',
    legend=dict(
        x=0.02, y=0.98,
        bgcolor='rgba(255,255,255,0.8)',
        bordercolor='#E5E5E5',
        borderwidth=1
    )
)

fig.update_xaxes(gridcolor='#E5E5E5', dtick=1)
fig.update_yaxes(gridcolor='#E5E5E5')

# Save
fig.write_image('offline_accrual.png', width=1200, height=700)
fig.write_image('offline_accrual.svg', format='svg')

print("✓ Offline accrual visualization created")


# Create a second chart showing comparison: with penalty vs without
fig2 = go.Figure()

rate = 50.0  # Mid-late game income rate
hours = np.linspace(0, 8, 100)

# With penalty (actual)
earnings_with_penalty = [calculate_offline_earnings(rate, h)[0] for h in hours]

# Without penalty (theoretical 100%)
earnings_without_penalty = [min(h, 4) * 3600 * rate for h in hours]

# Without cap (theoretical unlimited 70%)
earnings_no_cap = [h * 3600 * rate * 0.7 for h in hours]

fig2.add_trace(go.Scatter(
    x=hours, y=earnings_with_penalty,
    mode='lines', name='Actual (4h cap, 70% efficiency)',
    line=dict(color='#2E8B57', width=3)
))

fig2.add_trace(go.Scatter(
    x=hours, y=earnings_without_penalty,
    mode='lines', name='No Penalty (4h cap, 100% efficiency)',
    line=dict(color='#1FB8CD', width=2, dash='dash')
))

fig2.add_trace(go.Scatter(
    x=hours, y=earnings_no_cap,
    mode='lines', name='No Cap (unlimited time, 70% efficiency)',
    line=dict(color='#DB4545', width=2, dash='dot')
))

fig2.update_layout(
    title=f"Offline Earnings: Actual vs Theoretical<br><sub>Base income: ₹{rate}/s</sub>",
    xaxis_title="Offline Time (hours)",
    yaxis_title="Earnings (₹)",
    plot_bgcolor='white',
    hovermode='x unified'
)

fig2.update_xaxes(gridcolor='#E5E5E5', dtick=1)
fig2.update_yaxes(gridcolor='#E5E5E5')

fig2.write_image('offline_accrual_comparison.png', width=1200, height=700)
fig2.write_image('offline_accrual_comparison.svg', format='svg')

print("✓ Offline accrual comparison chart created")
