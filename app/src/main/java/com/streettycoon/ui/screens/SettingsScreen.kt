package com.streettycoon.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.streettycoon.audio.AudioManager
import com.streettycoon.ui.components.InfoCard
import com.streettycoon.ui.components.PrimaryButton
import com.streettycoon.ui.components.AnimatedAuroraBackground
import com.streettycoon.ui.theme.Colors
import com.streettycoon.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    audioManager: AudioManager,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isMusicEnabled by audioManager.isMusicEnabled.collectAsState()
    val isSoundEffectsEnabled by audioManager.isSoundEffectsEnabled.collectAsState()
    val musicVolume by audioManager.musicVolume.collectAsState()
    val soundEffectsVolume by audioManager.soundEffectsVolume.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        color = androidx.compose.ui.graphics.Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            "Back",
                            tint = androidx.compose.ui.graphics.Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Colors.OrangePrimary
                )
            )
        }
    ) { padding ->
        AnimatedAuroraBackground(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.xl),
                verticalArrangement = Arrangement.spacedBy(Spacing.xl)
            ) {
            item {
                Text(
                    text = "🔊 Audio Settings",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.White
                )
            }

            item {
                InfoCard(
                    title = "Background Music",
                    subtitle = if (isMusicEnabled) "Enabled" else "Disabled"
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Spacing.lg),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Colors.OrangePrimary
                        )
                        Switch(
                            checked = isMusicEnabled,
                            onCheckedChange = {
                                if (it) {
                                    audioManager.enableMusic()
                                    audioManager.startMusic()
                                } else {
                                    audioManager.disableMusic()
                                }
                            }
                        )
                    }
                }
            }

            if (isMusicEnabled) {
                item {
                    InfoCard(
                        title = "Music Volume",
                        subtitle = "${(musicVolume * 100).toInt()}%"
                    ) {
                        Row(
                            modifier = Modifier.padding(top = Spacing.lg),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeOff,
                                contentDescription = null,
                                tint = Colors.OrangePrimary
                            )

                            Slider(
                                value = musicVolume,
                                onValueChange = { audioManager.setMusicVolume(it) },
                                modifier = Modifier.weight(1f),
                                valueRange = 0f..1f
                            )

                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = Colors.OrangePrimary
                            )
                        }
                    }
                }
            }

            item {
                InfoCard(
                    title = "Sound Effects",
                    subtitle = if (isSoundEffectsEnabled) "Enabled" else "Disabled"
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Spacing.lg),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔊",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(end = Spacing.md)
                        )
                        Switch(
                            checked = isSoundEffectsEnabled,
                            onCheckedChange = {
                                if (it) {
                                    audioManager.enableSoundEffects()
                                } else {
                                    audioManager.disableSoundEffects()
                                }
                            }
                        )
                    }
                }
            }

            if (isSoundEffectsEnabled) {
                item {
                    InfoCard(
                        title = "Effects Volume",
                        subtitle = "${(soundEffectsVolume * 100).toInt()}%"
                    ) {
                        Row(
                            modifier = Modifier.padding(top = Spacing.lg),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeOff,
                                contentDescription = null,
                                tint = Colors.OrangePrimary
                            )

                            Slider(
                                value = soundEffectsVolume,
                                onValueChange = { audioManager.setSoundEffectsVolume(it) },
                                modifier = Modifier.weight(1f),
                                valueRange = 0f..1f
                            )

                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = Colors.OrangePrimary
                            )
                        }
                    }
                }
            }

            if (isSoundEffectsEnabled) {
                item {
                    PrimaryButton(
                        onClick = { audioManager.playCoinCollect() },
                        text = "Test Sound Effect"
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(Spacing.md))
                Text(
                    text = "ℹ️ About App",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.White
                )
            }

            item {
                InfoCard(
                    title = "Street Tycoon",
                    subtitle = "Version 1.0.0"
                ) {
                    Text(
                        text = "Build your street food empire with strategic management, character development, and family life simulation.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = Spacing.lg)
                    )
                }
            }
            }
        }
    }
}
