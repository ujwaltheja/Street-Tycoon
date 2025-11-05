import plotly.graph_objects as go
from plotly.subplots import make_subplots
import numpy as np

# Performance targets and benchmarks for Street Tycoon
# Based on architecture specifications

# Create subplots
fig = make_subplots(
    rows=2, cols=2,
    subplot_titles=(
        'Tick Loop Latency Targets',
        'Memory Usage by Device Tier',
        'JNI Call Overhead Distribution',
        'Frame Rate (FPS) Targets'
    ),
    specs=[[{"type": "bar"}, {"type": "bar"}],
           [{"type": "violin"}, {"type": "indicator"}]]
)

# Chart 1: Tick loop latency targets
operations = ['Tick (Native)', 'JNI Crossing', 'JSON Parse', 'UI Update', 'Total Tick']
latencies_ms = [0.5, 1.5, 2.0, 3.0, 7.0]  # Target latencies
max_latencies = [1.0, 2.5, 4.0, 5.0, 10.0]  # Max acceptable

fig.add_trace(
    go.Bar(
        x=operations,
        y=latencies_ms,
        name='Target',
        marker=dict(color='#2E8B57'),
        text=[f'{l}ms' for l in latencies_ms],
        textposition='outside'
    ),
    row=1, col=1
)

fig.add_trace(
    go.Bar(
        x=operations,
        y=max_latencies,
        name='Max Acceptable',
        marker=dict(color='#DB4545', opacity=0.5),
        text=[f'{l}ms' for l in max_latencies],
        textposition='outside'
    ),
    row=1, col=1
)

# Chart 2: Memory usage by device tier
devices = ['Low-end\n(512MB RAM)', 'Low-end\n(1GB RAM)', 'Mid-range\n(2GB RAM)', 'High-end\n(4GB+ RAM)']
memory_usage_mb = [45, 60, 85, 120]  # Target memory footprint
memory_colors = ['#DB4545', '#D2BA4C', '#1FB8CD', '#2E8B57']

fig.add_trace(
    go.Bar(
        x=devices,
        y=memory_usage_mb,
        marker=dict(color=memory_colors),
        text=[f'{m}MB' for m in memory_usage_mb],
        textposition='outside',
        showlegend=False
    ),
    row=1, col=2
)

# Chart 3: JNI call overhead distribution (simulated realistic distribution)
np.random.seed(42)
jni_overhead_samples = np.concatenate([
    np.random.normal(1.2, 0.3, 80),  # Most calls are fast
    np.random.normal(2.5, 0.5, 15),  # Some medium
    np.random.normal(4.0, 0.8, 5)    # Few slow outliers
])

fig.add_trace(
    go.Violin(
        y=jni_overhead_samples,
        name='JNI Overhead',
        marker=dict(color='#1FB8CD'),
        box_visible=True,
        meanline_visible=True,
        showlegend=False
    ),
    row=2, col=1
)

# Chart 4: FPS gauge
target_fps = 60

fig.add_trace(
    go.Indicator(
        mode="gauge+number+delta",
        value=60,
        title={'text': "Target FPS"},
        delta={'reference': 30},
        gauge={
            'axis': {'range': [None, 120]},
            'bar': {'color': "#2E8B57"},
            'steps': [
                {'range': [0, 30], 'color': "#DB4545"},
                {'range': [30, 60], 'color': "#D2BA4C"},
                {'range': [60, 120], 'color': "#2E8B57"}
            ],
            'threshold': {
                'line': {'color': "red", 'width': 4},
                'thickness': 0.75,
                'value': 60
            }
        }
    ),
    row=2, col=2
)

# Update layout
fig.update_layout(
    title_text="Street Tycoon: Performance Benchmarks & Targets",
    height=900,
    showlegend=True,
    plot_bgcolor='white'
)

# Update axes
fig.update_xaxes(tickangle=45, row=1, col=1)
fig.update_xaxes(tickangle=45, row=1, col=2)

fig.update_yaxes(title_text="Latency (ms)", gridcolor='#E5E5E5', range=[0, 12], row=1, col=1)
fig.update_yaxes(title_text="Memory (MB)", gridcolor='#E5E5E5', row=1, col=2)
fig.update_yaxes(title_text="Overhead (ms)", gridcolor='#E5E5E5', row=2, col=1)

# Save
fig.write_image('performance_benchmarks.png', width=1400, height=900)
fig.write_image('performance_benchmarks.svg', format='svg')

print("✓ Performance benchmarks chart created")


# Create a second detailed chart: Performance over time simulation
fig2 = go.Figure()

time_seconds = np.arange(0, 61)  # 60 seconds of gameplay

# Simulate realistic performance metrics
# Most ticks are fast, occasional GC pauses
np.random.seed(42)
base_latency = 5.0  # Base tick latency in ms
tick_latencies = base_latency + np.random.normal(0, 1.5, len(time_seconds))

# Add occasional GC pauses
gc_pause_indices = [10, 25, 45, 58]
for idx in gc_pause_indices:
    if idx < len(tick_latencies):
        tick_latencies[idx] += np.random.uniform(15, 30)

# Plot tick latency over time
fig2.add_trace(go.Scatter(
    x=time_seconds,
    y=tick_latencies,
    mode='lines',
    name='Tick Latency',
    line=dict(color='#1FB8CD', width=2),
    fill='tozeroy',
    fillcolor='rgba(31, 184, 205, 0.2)'
))

# Add threshold line
fig2.add_hline(
    y=10, line_dash="dash", line_color="red",
    annotation_text="10ms Target", annotation_position="right"
)

# Annotate GC pauses
for idx in gc_pause_indices:
    if idx < len(tick_latencies):
        fig2.add_annotation(
            x=idx, y=tick_latencies[idx],
            text="GC",
            showarrow=True,
            arrowhead=2,
            ax=20, ay=-30
        )

fig2.update_layout(
    title="Tick Latency Over Time (60 second session)<br><sub>Showing realistic performance with occasional GC pauses</sub>",
    xaxis_title="Time (seconds)",
    yaxis_title="Latency (ms)",
    plot_bgcolor='white',
    hovermode='x unified'
)

fig2.update_xaxes(gridcolor='#E5E5E5')
fig2.update_yaxes(gridcolor='#E5E5E5')

fig2.write_image('performance_over_time.png', width=1200, height=600)
fig2.write_image('performance_over_time.svg', format='svg')

print("✓ Performance over time chart created")
