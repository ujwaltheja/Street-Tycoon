package com.streettycoon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.streettycoon.ui.GameViewModel
import com.streettycoon.ui.navigation.StreetTycoonApp
import com.streettycoon.ui.theme.StreetTycoonTheme

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            StreetTycoonTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StreetTycoonApp(viewModel = gameViewModel)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        gameViewModel.saveGame()
    }
}
