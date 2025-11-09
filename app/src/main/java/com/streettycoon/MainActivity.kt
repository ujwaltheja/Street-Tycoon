package com.streettycoon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.graphicsLayer
import android.util.Log
import com.streettycoon.ui.GameViewModel
import com.streettycoon.ui.navigation.StreetTycoonApp
import com.streettycoon.ui.theme.StreetTycoonTheme
import com.streettycoon.sound.SoundManager
import com.streettycoon.ui.components.AnimatedAuroraBackground
import com.streettycoon.ui.components.CityHeroIllustration
import com.streettycoon.ui.components.PrimaryButton
import com.streettycoon.ui.components.TertiaryButton

// Using default system font (custom Bungee font file not available)
val BungeeRegular = FontFamily.Default

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_StreetTycoon)
        super.onCreate(savedInstanceState)

        // Initialize SoundManager with error handling
        try {
            SoundManager.getInstance(this)
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to initialize SoundManager", e)
        }

        setContent {
            val context = LocalContext.current
            val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
            var showGame by remember { mutableStateOf(false) }

            // Initialize SoundManager and manage lifecycle
            DisposableEffect(lifecycleOwner) {
                val soundManager = try {
                    SoundManager.getInstance(context)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Failed to get SoundManager instance", e)
                    null
                }
                val observer = LifecycleEventObserver { _, event ->
                    try {
                        when (event) {
                            Lifecycle.Event.ON_RESUME -> {
                                soundManager?.resumeBackgroundMusic()
                            }
                            Lifecycle.Event.ON_PAUSE -> {
                                soundManager?.pauseBackgroundMusic()
                            }
                            Lifecycle.Event.ON_DESTROY -> {
                                soundManager?.release()
                            }
                            else -> {}
                        }
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error in lifecycle event handler", e)
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
                    // Check for native library error
                    val nativeError by gameViewModel.nativeLibraryError.collectAsState()

                    if (nativeError != null) {
                        // Show error screen
                        NativeLibraryErrorScreen(errorMessage = nativeError ?: "Unknown error")
                    } else if (showGame) {
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
    val soundManager = remember {
        try {
            SoundManager.getInstance(context)
        } catch (e: Exception) {
            Log.e("MainScreen", "Failed to get SoundManager instance", e)
            null
        }
    }

    AnimatedAuroraBackground(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            CityHeroIllustration(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(260.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "main_title_pulse")
                val titleScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1600, easing = LinearOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "main_title_scale"
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(28.dp),
                    tonalElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Street Tycoon",
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = BungeeRegular,
                            color = Color.White,
                            modifier = Modifier.graphicsLayer(scaleX = titleScale, scaleY = titleScale)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Craft a neon-soaked food empire with bold stalls, elite crews, and family legacy.",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    var buttonScale by remember { mutableStateOf(1f) }
                    val animatedButtonScale by animateFloatAsState(
                        targetValue = buttonScale,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "start_button_scale"
                    )
                    PrimaryButton(
                        text = "Enter The Street",
                        onClick = {
                            buttonScale = if (buttonScale == 1f) 1.08f else 1f
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            try {
                                soundManager?.playTapSound()
                            } catch (e: Exception) {
                                Log.e("MainScreen", "Error playing tap sound", e)
                            }
                            onStartGame()
                        },
                        modifier = Modifier
                            .graphicsLayer(scaleX = animatedButtonScale, scaleY = animatedButtonScale)
                    )

                    TertiaryButton(
                        text = "Exit Game",
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            try {
                                soundManager?.playTapSound()
                            } catch (e: Exception) {
                                Log.e("MainScreen", "Error playing tap sound", e)
                            }
                            showExitDialog = true
                        }
                    )
                }
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

@Composable
fun NativeLibraryErrorScreen(errorMessage: String) {
    val context = LocalContext.current
    val activity = (context as? ComponentActivity)

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
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            Text(
                text = "Street Tycoon",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = BungeeRegular,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Text(
                text = "Failed to Start Game",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "The game engine could not be loaded. This may happen if:",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "• The app was not built with NDK support\n• Native library is missing or corrupted\n• Your device architecture is not supported",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Error: $errorMessage",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Button(
                onClick = {
                    activity?.finish()
                },
                shape = MaterialTheme.shapes.medium,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("Close App", fontSize = 18.sp)
            }
        }
    }
}
