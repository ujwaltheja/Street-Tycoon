package com.streettycoon

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.draw.scale
import com.streettycoon.ui.GameViewModel
import com.streettycoon.ui.navigation.StreetTycoonApp
import com.streettycoon.ui.theme.StreetTycoonTheme
import com.streettycoon.sound.SoundManager

// Using default system font (custom Bungee font file not available)
val BungeeRegular = FontFamily.Default

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_StreetTycoon)
        super.onCreate(savedInstanceState)

        // Initialize SoundManager
        SoundManager.getInstance(this)

        setContent {
            val context = LocalContext.current
            val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
            var showGame by remember { mutableStateOf(false) }

            // Initialize SoundManager and manage lifecycle
            DisposableEffect(lifecycleOwner) {
                val soundManager = SoundManager.getInstance(context)
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> {
                            soundManager.resumeBackgroundMusic()
                        }
                        Lifecycle.Event.ON_PAUSE -> {
                            soundManager.pauseBackgroundMusic()
                        }
                        Lifecycle.Event.ON_DESTROY -> {
                            soundManager.release()
                        }
                        else -> {}
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            StreetTycoonTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (showGame) {
                        StreetTycoonApp(viewModel = gameViewModel)
                    } else {
                        MainScreen(onStartGame = { showGame = true })
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        gameViewModel.saveGame()
    }
}

@Composable
fun MainScreen(onStartGame: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val activity = (context as? ComponentActivity)
    var showExitDialog: Boolean by remember { mutableStateOf(false) }

    // Initialize SoundManager for sound effects
    val soundManager = remember { SoundManager.getInstance(context) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Gradient background (replacing painterResource which doesn't support shape drawables)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFF6B35),
                            Color(0xFFFFD45B)
                        )
                    )
                )
        )
        // Dark overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "title_pulse_transition")
            val titleScale: Float by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ), label = "title_scale_animation"
            )
            Text(
                text = "Street Tycoon",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = BungeeRegular,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(bottom = 32.dp)
                    .scale(titleScale)
            )
            var buttonScale: Float by remember { mutableStateOf(1f) }
            val animatedButtonScale: Float by animateFloatAsState(
                targetValue = buttonScale,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "button_scale_animation"
            )
            Button(
                onClick = {
                    buttonScale = if (buttonScale == 1f) 1.1f else 1f
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    soundManager.playTapSound()
                    onStartGame()
                },
                modifier = Modifier
                    .scale(animatedButtonScale)
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                shape = MaterialTheme.shapes.medium,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Start Game", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    soundManager.playTapSound()
                    showExitDialog = true
                },
                modifier = Modifier
                    .padding(horizontal = 32.dp, vertical = 8.dp),
                shape = MaterialTheme.shapes.medium,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("Exit Game", fontSize = 20.sp)
            }
        }
    }
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Exit Game") },
            text = { Text("Are you sure you want to exit Street Tycoon?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        activity?.finish()
                    }
                ) { Text("Yes") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExitDialog = false }
                ) { Text("No") }
            }
        )
    }
}
