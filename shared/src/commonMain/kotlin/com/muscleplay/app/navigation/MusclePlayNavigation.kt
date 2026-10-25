package com.muscleplay.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.muscleplay.app.core.di.AppContainer
import com.muscleplay.app.feature.calculators.CalculatorsScreen
import com.muscleplay.app.feature.games.GamesScreen
import com.muscleplay.app.feature.games.RepChallengeScreen
import com.muscleplay.app.feature.games.pushup.PushUpCameraScreen
import com.muscleplay.app.feature.games.squat.SquatCameraScreen
import com.muscleplay.app.feature.home.HomeScreen
import com.muscleplay.app.feature.profile.FullPhotoGalleryScreen
import com.muscleplay.app.feature.profile.HelpSupportScreen
import com.muscleplay.app.feature.profile.MyTicketsScreen
import com.muscleplay.app.feature.profile.ProfileScreen
import com.muscleplay.app.feature.profile.RaiseTicketScreen
import com.muscleplay.app.feature.profile.SelfDiagnosticsScreen
import com.muscleplay.app.feature.profile.SettingsScreen
import com.muscleplay.app.feature.workout.WorkoutScreen

private val OrangePrimary = Color(0xFFFF5722).copy(alpha = 0.8f)
private val OrangeSecondary = Color(0xFFFF9800).copy(alpha = 0.5f)
private val DarkBackground = Color(0xFF0F0F17)

private object Routes {
    const val SQUAT_MASTER_ROUTE = "squat_master"
    const val REP_CHALLENGE_ROUTE = "rep_challenge"
    const val PUSH_UP_MASTER_ROUTE = "push_up_master"

    const val PROFILE_GALLERY = "profile_gallery"
    const val PROFILE_DIAGNOSTICS = "profile_diagnostics"
    const val PROFILE_HELP = "profile_help"
    const val PROFILE_SETTINGS = "profile_settings"
    const val PROFILE_RAISE_TICKET = "profile_raise_ticket"
    const val PROFILE_MY_TICKETS = "profile_my_tickets"

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

    val bottomBarHiddenRoutes = setOf(
        Routes.SQUAT_MASTER_ROUTE,
        Routes.REP_CHALLENGE_ROUTE,
        Routes.PUSH_UP_MASTER_ROUTE,
        Routes.PROFILE_GALLERY,
        Routes.PROFILE_DIAGNOSTICS,
        Routes.PROFILE_HELP,
        Routes.PROFILE_SETTINGS,
        Routes.PROFILE_RAISE_TICKET,
        Routes.PROFILE_MY_TICKETS
    )
    val shouldHideBottomBar = currentRoute in bottomBarHiddenRoutes

    Scaffold(
        containerColor = DarkBackground, bottomBar = {
            if (!shouldHideBottomBar) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(32.dp)).background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xEE1A1A26), Color(0xEE12121B)
                                )
                            )
                        ).border(
                            width = 1.dp, brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.35f),
                                    OrangePrimary.copy(alpha = 0.3f),
                                    Color.White.copy(alpha = 0.08f)
                                )
                            ), shape = RoundedCornerShape(32.dp)
                        )
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent, tonalElevation = 0.dp
                    ) {
                        tabs.forEach { tab ->
                            val selected = currentDestination?.hierarchy?.any {
                                it.route == tab.route
                            } == true

                            NavigationBarItem(
                                selected = selected, onClick = {
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
                            }, label = {
                                Text(
                                    text = tab.title,
                                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
                                )
                            }, colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = OrangePrimary,
                                indicatorColor = OrangePrimary,
                                unselectedIconColor = Color.White.copy(alpha = 0.45f),
                                unselectedTextColor = Color.White.copy(alpha = 0.45f)
                            )
                            )
                        }
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
                WorkoutScreen(
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

                            Routes.PUSH_UP_MASTER_ROUTE -> {
                                navController.navigate(Routes.PUSH_UP_MASTER_ROUTE)
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
                ProfileScreen(
                    onNavigateToSettings = { navController.navigate(Routes.PROFILE_SETTINGS) },
                    onNavigateToHelp = { navController.navigate(Routes.PROFILE_HELP) },
                    onNavigateToDiagnostics = { navController.navigate(Routes.PROFILE_DIAGNOSTICS) },
                    onNavigateToGallery = { navController.navigate(Routes.PROFILE_GALLERY) })
            }
            composable(Routes.PROFILE_GALLERY) {
                FullPhotoGalleryScreen(
                    onBack = {
                        navController.popBackStack()
                    })
            }
            composable(Routes.PROFILE_DIAGNOSTICS) {
                SelfDiagnosticsScreen(
                    onBack = {
                        navController.popBackStack()
                    })
            }
            composable(Routes.PROFILE_HELP) {
                HelpSupportScreen(onBack = {
                    navController.popBackStack()
                }, onNavigateToRaiseTicket = {
                    navController.navigate(Routes.PROFILE_RAISE_TICKET)
                }, onNavigateToMyTickets = {
                    navController.navigate(Routes.PROFILE_MY_TICKETS)
                })
            }
            composable(Routes.PROFILE_SETTINGS) {
                SettingsScreen(
                    onBack = {
                        navController.popBackStack()
                    })
            }
            composable(Routes.PROFILE_RAISE_TICKET) {
                RaiseTicketScreen(onBack = {
                    navController.popBackStack()
                }, onViewTickets = {
                    navController.popBackStack()
                    navController.navigate(Routes.PROFILE_MY_TICKETS)
                })
            }
            composable(Routes.PROFILE_MY_TICKETS) {
                MyTicketsScreen(onBack = {
                    navController.popBackStack()
                }, onRaiseTicket = {
                    navController.navigate(Routes.PROFILE_RAISE_TICKET)
                })
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
            composable(Routes.PUSH_UP_MASTER_ROUTE) {
                PushUpCameraScreen(
                    onBack = {
                        navController.popBackStack()
                    })
            }
        }
    }
}
