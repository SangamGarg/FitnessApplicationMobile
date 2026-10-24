package com.muscleplay.app.navigation

import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavDestination.Companion.hierarchy
import com.muscleplay.app.core.di.AppContainer
import com.muscleplay.app.feature.calculators.CalculatorsScreen
import com.muscleplay.app.feature.games.GamesScreen
import com.muscleplay.app.feature.games.RepChallengeScreen
import com.muscleplay.app.feature.games.SquatCameraScreen
import com.muscleplay.app.feature.home.HomeScreen
import com.muscleplay.app.feature.profile.ProfileScreen
import com.muscleplay.app.feature.workouts.WorkoutsScreen

private object Routes {
    const val SQUAT_MASTER_ROUTE = "squat_master"
    const val REP_CHALLENGE_ROUTE = "rep_challenge"
    const val HOME = "home"
    const val WORKOUTS = "workouts"
    const val GAMES = "games"
    const val CALCULATORS = "calculators"
    const val PROFILE = "profile"
}

private data class TabItem(
    val route: String,
    val title: String,
)

private val tabs = listOf(
    TabItem(Routes.HOME, "Home"),
    TabItem(Routes.WORKOUTS, "Workouts"),
    TabItem(Routes.GAMES, "Games"),
    TabItem(Routes.CALCULATORS, "Calculators"),
    TabItem(Routes.PROFILE, "Profile")
)

@Composable
fun MusclePlayNavigation(
    appContainer: AppContainer,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isSquatMasterScreen = currentRoute == Routes.SQUAT_MASTER_ROUTE
    val bottomBarHiddenRoutes = setOf(
        Routes.SQUAT_MASTER_ROUTE, Routes.REP_CHALLENGE_ROUTE
    )
    val shouldHideBottomBar = currentRoute in bottomBarHiddenRoutes

    Scaffold(
        bottomBar = {
            if (!shouldHideBottomBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.route == tab.route
                        } == true

                        NavigationBarItem(selected = selected, onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(
                                    navController.graph.findStartDestination().id
                                ) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }, icon = {
                            Icon(
                                imageVector = when (tab.route) {
                                    Routes.HOME -> Icons.Filled.Home
                                    Routes.WORKOUTS -> Icons.Filled.FitnessCenter
                                    Routes.GAMES -> Icons.Filled.Gamepad
                                    Routes.CALCULATORS -> Icons.Filled.Calculate
                                    else -> Icons.Filled.Person
                                }, contentDescription = tab.title
                            )
                        }, label = { Text(tab.title) })
                    }
                }
            }
        }) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(
                bottom = if (shouldHideBottomBar) 0.dp else innerPadding.calculateBottomPadding(),
                top = innerPadding.calculateTopPadding()
            )
        ) {
            composable(Routes.HOME) {
                HomeScreen()
            }
            composable(Routes.WORKOUTS) {
                WorkoutsScreen(
                    repository = appContainer.workoutRepository
                )
            }
            composable(Routes.GAMES) {
                GamesScreen(
                    onPlayGame = { gameId ->
                        when (gameId) {
                            Routes.SQUAT_MASTER_ROUTE -> {
                                navController.navigate(Routes.SQUAT_MASTER_ROUTE)
                            }

                            Routes.REP_CHALLENGE_ROUTE -> {
                                navController.navigate(Routes.REP_CHALLENGE_ROUTE)
                            }

                            else -> {

                            }
                        }
                    })
            }
            composable(Routes.CALCULATORS) {
                CalculatorsScreen()
            }
            composable(Routes.PROFILE) {
                ProfileScreen()
            }
            composable(Routes.REP_CHALLENGE_ROUTE) {
                RepChallengeScreen(
                    onBack = {
                        navController.popBackStack()
                    })
            }
            composable(Routes.SQUAT_MASTER_ROUTE) {
                SquatCameraScreen(
                    onBack = {
                        navController.popBackStack()
                    })
            }
        }
    }
}
