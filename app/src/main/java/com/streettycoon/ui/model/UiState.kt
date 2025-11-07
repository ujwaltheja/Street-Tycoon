package com.streettycoon.ui.model

import com.streettycoon.game.model.GameState

/**
 * Sealed class representing different UI states
 * Used for better error handling and loading states
 */
sealed class UiState {
    /**
     * Loading state - game is initializing
     */
    data class Loading(val message: String = "Loading...") : UiState()

    /**
     * Success state - game is ready to play
     */
    data class Success(val gameState: GameState) : UiState()

    /**
     * Error state - something went wrong
     */
    data class Error(
        val message: String,
        val exception: Exception? = null,
        val canRetry: Boolean = true
    ) : UiState()

    /**
     * Empty state - no data available
     */
    object Empty : UiState()
}

/**
 * Helper extension to check if state is loading
 */
fun UiState.isLoading(): Boolean = this is UiState.Loading

/**
 * Helper extension to check if state is success
 */
fun UiState.isSuccess(): Boolean = this is UiState.Success

/**
 * Helper extension to check if state is error
 */
fun UiState.isError(): Boolean = this is UiState.Error

/**
 * Helper extension to get game state if success
 */
fun UiState.getGameStateOrNull(): GameState? = when (this) {
    is UiState.Success -> gameState
    else -> null
}
