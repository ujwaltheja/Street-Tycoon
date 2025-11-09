package com.streettycoon.tutorial

import android.content.Context
import android.util.Log
import com.streettycoon.analytics.EventTracker
import com.streettycoon.analytics.GameEvent
import com.streettycoon.data.GameRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Manages tutorial state and progression
 * Tracks which steps have been completed and triggers appropriate UI
 */
class TutorialManager(private val context: Context) {

    private val repository = GameRepository(context)
    private val eventTracker = EventTracker.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.Main)

    // Current tutorial state
    private val _currentStep = MutableStateFlow<TutorialStep?>(null)
    val currentStep: StateFlow<TutorialStep?> = _currentStep.asStateFlow()

    private val _tutorialActive = MutableStateFlow(false)
    val tutorialActive: StateFlow<Boolean> = _tutorialActive.asStateFlow()

    private val _tutorialCompleted = MutableStateFlow(false)
    val tutorialCompleted: StateFlow<Boolean> = _tutorialCompleted.asStateFlow()

    private val completedSteps = mutableSetOf<String>()
    private var tutorialStartTime: Long = 0

    /**
     * Initialize tutorial system
     * Checks if tutorial should be shown to the player
     */
    suspend fun initialize() {
        val prefs = repository.loadPreferences()
        _tutorialCompleted.value = prefs.tutorialCompleted

        if (!prefs.tutorialCompleted) {
            // New player - start tutorial
            startTutorial()
        }
    }

    /**
     * Start the tutorial
     */
    fun startTutorial() {
        Log.d(TAG, "Starting tutorial")
        tutorialStartTime = System.currentTimeMillis()
        _tutorialActive.value = true
        completedSteps.clear()

        // Log analytics
        eventTracker.logEvent(
            GameEvent.TutorialStarted(
                tutorialStep = TutorialStep.TapIntro.id
            )
        )

        // Show first step
        showStep(TutorialStep.TapIntro)
    }

    /**
     * Show a specific tutorial step
     */
    fun showStep(step: TutorialStep) {
        Log.d(TAG, "Showing tutorial step: ${step.id}")
        _currentStep.value = step
    }

    /**
     * Complete current step and move to next
     */
    fun completeCurrentStep(nextStep: TutorialStep? = null) {
        val current = _currentStep.value ?: return

        // Mark step as completed
        completedSteps.add(current.id)

        // Log analytics
        val stepDuration = System.currentTimeMillis() - tutorialStartTime
        eventTracker.logEvent(
            GameEvent.TutorialStepCompleted(
                tutorialStep = current.id,
                timeSpentSeconds = stepDuration / 1000
            )
        )

        Log.d(TAG, "Completed step: ${current.id}")

        // Move to next step or complete tutorial
        if (nextStep != null) {
            showStep(nextStep)
        } else {
            // Check if all core steps are complete
            val coreSteps = TutorialStep.getCoreSteps()
            val allCoreCompleted = coreSteps.all { completedSteps.contains(it.id) }

            if (allCoreCompleted || current is TutorialStep.TutorialComplete) {
                completeTutorial()
            } else {
                // Auto-advance to next step
                val allSteps = TutorialStep.getCoreSteps()
                val currentIndex = allSteps.indexOfFirst { it.id == current.id }
                if (currentIndex >= 0 && currentIndex < allSteps.size - 1) {
                    showStep(allSteps[currentIndex + 1])
                }
            }
        }
    }

    /**
     * Complete the entire tutorial
     */
    private fun completeTutorial() {
        Log.d(TAG, "Tutorial completed")
        _tutorialActive.value = false
        _tutorialCompleted.value = true
        _currentStep.value = null

        // Save completion to preferences
        scope.launch {
            repository.markTutorialCompleted()
        }

        // Log analytics
        val totalDuration = System.currentTimeMillis() - tutorialStartTime
        eventTracker.logEvent(
            GameEvent.TutorialCompleted(
                totalDurationSeconds = totalDuration / 1000,
                stepsCompleted = completedSteps.size
            )
        )
    }

    /**
     * Skip tutorial
     */
    fun skipTutorial() {
        Log.d(TAG, "Tutorial skipped")

        val currentStepId = _currentStep.value?.id ?: "unknown"

        _tutorialActive.value = false
        _tutorialCompleted.value = true
        _currentStep.value = null

        // Save to preferences
        scope.launch {
            repository.markTutorialCompleted()
        }

        // Log analytics
        eventTracker.logEvent(
            GameEvent.TutorialSkipped(
                atStep = currentStepId
            )
        )
    }

    /**
     * Reset tutorial (for testing or replay)
     */
    fun resetTutorial() {
        scope.launch {
            val prefs = repository.loadPreferences()
            repository.savePreferences(prefs.copy(tutorialCompleted = false))
            _tutorialCompleted.value = false
            completedSteps.clear()
        }
    }

    /**
     * Show advanced tutorial step when triggered by game event
     * Example: Show family tutorial when player reaches ₹5,000
     */
    fun showAdvancedStep(step: TutorialStep) {
        if (_tutorialCompleted.value && !completedSteps.contains(step.id)) {
            Log.d(TAG, "Showing advanced tutorial: ${step.id}")
            _currentStep.value = step
            _tutorialActive.value = true

            // Auto-hide after showing
            scope.launch {
                kotlinx.coroutines.delay(5000) // Show for 5 seconds
                _currentStep.value = null
                _tutorialActive.value = false
                completedSteps.add(step.id)
            }
        }
    }

    /**
     * Check if specific step has been completed
     */
    fun isStepCompleted(stepId: String): Boolean {
        return completedSteps.contains(stepId)
    }

    companion object {
        private const val TAG = "TutorialManager"

        @Volatile
        private var INSTANCE: TutorialManager? = null

        fun getInstance(context: Context): TutorialManager {
            return INSTANCE ?: synchronized(this) {
                val instance = TutorialManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
