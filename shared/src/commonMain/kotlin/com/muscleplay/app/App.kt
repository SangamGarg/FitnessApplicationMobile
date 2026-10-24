package com.muscleplay.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.muscleplay.app.core.di.AppContainer
import com.muscleplay.app.navigation.MusclePlayNavigation

@Composable
fun App() {
    val appContainer = remember { AppContainer() }

    MaterialTheme {
        MusclePlayNavigation(
            appContainer = appContainer
        )
    }
}
