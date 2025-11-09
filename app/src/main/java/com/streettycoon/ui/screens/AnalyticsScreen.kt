package com.streettycoon.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.streettycoon.analytics.AnalyticsRepository
import com.streettycoon.analytics.GameStats
import com.streettycoon.data.GameEventEntity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Analytics screen for viewing game statistics
 * Shows local analytics data and allows CSV export
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    repository: AnalyticsRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var stats by remember { mutableStateOf<GameStats?>(null) }
    var recentEvents by remember { mutableStateOf<List<GameEventEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()

    // Load analytics data
    LaunchedEffect(Unit) {
        isLoading = true
        stats = repository.getGameStats()
        recentEvents = repository.getRecentEvents(50)
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Game Analytics") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Statistics") },
                    icon = { Icon(Icons.Default.BarChart, null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Events") },
                    icon = { Icon(Icons.Default.List, null) }
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                when (selectedTab) {
                    0 -> StatsTab(stats = stats)
                    1 -> EventsTab(events = recentEvents)
                }
            }
        }
    }
}

@Composable
private fun StatsTab(stats: GameStats?) {
    if (stats == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No statistics available")
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Game Statistics",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            StatCard(
                title = "Total Sessions",
                value = stats.totalSessions.toString(),
                icon = Icons.Default.PlayCircle,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            StatCard(
                title = "Tutorial Completion Rate",
                value = String.format("%.1f%%", stats.tutorialCompletionRate),
                icon = Icons.Default.School,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        item {
            StatCard(
                title = "Zones Unlocked",
                value = stats.totalZonesUnlocked.toString(),
                icon = Icons.Default.Map,
                color = MaterialTheme.colorScheme.tertiary
            )
        }

        item {
            StatCard(
                title = "Total Helpers Hired",
                value = stats.totalHelpersHired.toString(),
                icon = Icons.Default.People,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            StatCard(
                title = "Total Upgrades",
                value = stats.totalUpgrades.toString(),
                icon = Icons.Default.Upgrade,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        item {
            StatCard(
                title = "Total Events Logged",
                value = stats.totalEvents.toString(),
                icon = Icons.Default.Event,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    color = color
                )
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = color.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun EventsTab(events: List<GameEventEntity>) {
    if (events.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EventNote,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
                Text(
                    text = "No events recorded yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Events",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "${events.size} events",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        items(events) { event ->
            EventCard(event = event)
        }
    }
}

@Composable
private fun EventCard(event: GameEventEntity) {
    val dateFormat = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
    val date = Date(event.timestamp)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (event.syncedToFirebase)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = getEventIcon(event.eventName),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = event.eventName.replace("_", " ").capitalize(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = dateFormat.format(date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }

            if (!event.syncedToFirebase) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = "Not synced",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }
    }
}

private fun getEventIcon(eventName: String): ImageVector {
    return when {
        eventName.contains("session") -> Icons.Default.PlayCircle
        eventName.contains("tutorial") -> Icons.Default.School
        eventName.contains("zone") -> Icons.Default.Map
        eventName.contains("helper") -> Icons.Default.People
        eventName.contains("upgrade") -> Icons.Default.Upgrade
        eventName.contains("tap") -> Icons.Default.TouchApp
        eventName.contains("character") -> Icons.Default.Person
        eventName.contains("family") -> Icons.Default.FamilyRestroom
        else -> Icons.Default.Event
    }
}

private fun String.capitalize(): String {
    return this.split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { it.uppercase() }
    }
}
