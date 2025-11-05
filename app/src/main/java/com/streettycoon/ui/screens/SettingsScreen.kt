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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.streettycoon.audio.AudioManager

/**
 * Settings screen for game configuration
 */
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
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Audio Section Header
            item {
                Text(
                    text = "Audio",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )
            }

            // Music Toggle
            item {
                SettingCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = Color(0xFF2196F3)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Background Music",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isMusicEnabled) "Enabled" else "Disabled",
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575)
                                )
                            }
                        }

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

            // Music Volume Slider
            if (isMusicEnabled) {
                item {
                    SettingCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Music Volume",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${(musicVolume * 100).toInt()}%",
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeOff,
                                    contentDescription = null,
                                    tint = Color(0xFF9E9E9E),
                                    modifier = Modifier.size(20.dp)
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
                                    tint = Color(0xFF2196F3),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Sound Effects Toggle
            item {
                SettingCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "🔊",
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Sound Effects",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isSoundEffectsEnabled) "Enabled" else "Disabled",
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575)
                                )
                            }
                        }

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

            // Sound Effects Volume Slider
            if (isSoundEffectsEnabled) {
                item {
                    SettingCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Effects Volume",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${(soundEffectsVolume * 100).toInt()}%",
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeOff,
                                    contentDescription = null,
                                    tint = Color(0xFF9E9E9E),
                                    modifier = Modifier.size(20.dp)
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
                                    tint = Color(0xFF2196F3),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Test Sound Button
            if (isSoundEffectsEnabled) {
                item {
                    Button(
                        onClick = { audioManager.playCoinCollect() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Text("Test Sound Effect")
                    }
                }
            }

            // About Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "About",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )
            }

            item {
                SettingCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Street Tycoon",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Version 1.0.0",
                            fontSize = 12.sp,
                            color = Color(0xFF757575)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Build your street food empire with strategic management, character development, and family life simulation.",
                            fontSize = 12.sp,
                            color = Color(0xFF616161)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card wrapper for settings items
 */
@Composable
private fun SettingCard(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        content()
    }
}
