package com.muscleplay.app.feature.workout

import androidx.compose.runtime.*
import com.muscleplay.app.feature.workout.model.*

enum class WorkoutScreenState {
    CATALOGUE,
    ACTIVE_PLAYER,
    SUMMARY
}

@Composable
fun WorkoutScreen(
    repository: WorkoutRepository? = null,
    onActiveSessionStateChange: (Boolean) -> Unit = {},
    onDone: () -> Unit = {}
) {
    var screenState by remember { mutableStateOf(WorkoutScreenState.CATALOGUE) }
    var selectedDetailExercise by remember { mutableStateOf<Exercise?>(null) }
    var activeSession by remember { mutableStateOf<ActiveWorkoutSession?>(null) }
    var initialMode by remember { mutableStateOf(PlayerTrackingMode.MANUAL) }
    var summaryData by remember { mutableStateOf<WorkoutSummaryData?>(null) }

    LaunchedEffect(screenState) {
        onActiveSessionStateChange(screenState != WorkoutScreenState.CATALOGUE)
    }

    when (screenState) {
        WorkoutScreenState.CATALOGUE -> {
            ExerciseCatalogueScreen(
                onSelectExercise = { exercise ->
                    selectedDetailExercise = exercise
                },
                onBack = onDone
            )

            selectedDetailExercise?.let { exercise ->
                ExerciseDetailBottomSheet(
                    exercise = exercise,
                    onDismissRequest = { selectedDetailExercise = null },
                    onStartExerciseManual = { selectedEx ->
                        selectedDetailExercise = null
                        activeSession = createDefaultSession(selectedEx)
                        initialMode = PlayerTrackingMode.MANUAL
                        screenState = WorkoutScreenState.ACTIVE_PLAYER
                    },
                    onStartExerciseAi = { selectedEx ->
                        selectedDetailExercise = null
                        activeSession = createDefaultSession(selectedEx)
                        initialMode = PlayerTrackingMode.AI_TRACKER
                        screenState = WorkoutScreenState.ACTIVE_PLAYER
                    }
                )
            }
        }

        WorkoutScreenState.ACTIVE_PLAYER -> {
            val session = activeSession ?: createDefaultSession(mockExercisesList.first())
            WorkoutPlayerScreen(
                session = session,
                initialTrackingMode = initialMode,
                onFinishSession = { summary ->
                    summaryData = summary
                    screenState = WorkoutScreenState.SUMMARY
                },
                onCancelSession = {
                    activeSession = null
                    screenState = WorkoutScreenState.CATALOGUE
                }
            )
        }

        WorkoutScreenState.SUMMARY -> {
            summaryData?.let { summary ->
                WorkoutSummaryScreen(
                    summary = summary,
                    onDone = {
                        summaryData = null
                        activeSession = null
                        screenState = WorkoutScreenState.CATALOGUE
                        onDone()
                    }
                )
            }
        }
    }
}
