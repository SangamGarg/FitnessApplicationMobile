package com.muscleplay.app.navigation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.muscleplay.app.core.di.AppContainer
import com.muscleplay.app.feature.auth.OtpVerificationScreen
import com.muscleplay.app.feature.auth.ResetPasswordScreen
import com.muscleplay.app.feature.auth.SignInScreen
import com.muscleplay.app.feature.auth.SignUpScreen
import com.muscleplay.app.feature.calculators.CalculatorsScreen
import com.muscleplay.app.feature.coins.MpCoinsHubScreen
import com.muscleplay.app.feature.diet.DietHomeScreen
import com.muscleplay.app.feature.diet.IngredientScannerScreen
import com.muscleplay.app.feature.games.GamesScreen
import com.muscleplay.app.feature.games.RepChallengeScreen
import com.muscleplay.app.feature.games.pushup.PushUpCameraScreen
import com.muscleplay.app.feature.games.squat.SquatCameraScreen
import com.muscleplay.app.feature.home.HomeScreen
import com.muscleplay.app.feature.onboarding.OnboardingScreen
import com.muscleplay.app.feature.onboarding.UserProfileSetupScreen
import com.muscleplay.app.feature.onboarding.rememberOnboardingPreferences
import com.muscleplay.app.feature.profile.ProfileScreen
import com.muscleplay.app.feature.profile.diagnostics.SelfDiagnosticsScreen
import com.muscleplay.app.feature.profile.gallery.FullPhotoGalleryScreen
import com.muscleplay.app.feature.profile.settings.SettingsScreen
import com.muscleplay.app.feature.profile.support.AiBotSupportScreen
import com.muscleplay.app.feature.profile.support.EmailSupportScreen
import com.muscleplay.app.feature.profile.support.HelpSupportScreen
import com.muscleplay.app.feature.profile.support.MyTicketsScreen
import com.muscleplay.app.feature.profile.support.RaiseTicketScreen
import com.muscleplay.app.feature.splash.SplashScreen
import com.muscleplay.app.feature.workout.WorkoutScreen

private val OrangePrimary = Color(0xFFFF5722)
private val OrangeSecondary = Color(0xFFFF9800)
private val DarkBackground = Color(0xFF0F0F17)

private object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val SIGN_IN = "sign_in"
    const val SIGN_UP = "sign_up"
    const val OTP_VERIFICATION = "otp_verification"
    const val USER_PROFILE_SETUP = "user_profile_setup"
    const val RESET_PASSWORD = "reset_password"

    const val DIET_HOME = "diet_home"
    const val INGREDIENT_SCANNER = "ingredient_scanner"

    const val SQUAT_MASTER_ROUTE = "squat_master"
    const val REP_CHALLENGE_ROUTE = "rep_challenge"
    const val PUSH_UP_MASTER_ROUTE = "push_up_master"

    const val PROFILE_GALLERY = "profile_gallery"
    const val PROFILE_DIAGNOSTICS = "profile_diagnostics"
    const val PROFILE_HELP = "profile_help"
    const val PROFILE_SETTINGS = "profile_settings"
    const val PROFILE_RAISE_TICKET = "profile_raise_ticket"
    const val PROFILE_MY_TICKETS = "profile_my_tickets"
    const val PROFILE_COIN_REDEMPTION = "profile_coin_redemption"
    const val PROFILE_EMAIL_SUPPORT = "profile_email_support"
    const val PROFILE_AI_BOT = "profile_ai_bot"

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

private fun getTabIcon(route: String): ImageVector {
    return when (route) {
        Routes.HOME -> Icons.Filled.Home
        Routes.WORKOUTS -> Icons.Filled.FitnessCenter
        Routes.GAMES -> Icons.Filled.Gamepad
        Routes.CALCULATORS -> Icons.Filled.Calculate
        else -> Icons.Filled.Person
    }
}

@Composable
fun MusclePlayNavigation(
    appContainer: AppContainer,
) {
    val navController = rememberNavController()
    val onboardingPreferences = rememberOnboardingPreferences()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val currentRoute = currentDestination?.route

    val bottomBarHiddenRoutes = setOf(
        Routes.SPLASH,
        Routes.ONBOARDING,
        Routes.SIGN_IN,
        Routes.SIGN_UP,
        Routes.OTP_VERIFICATION,
        Routes.USER_PROFILE_SETUP,
        Routes.RESET_PASSWORD,
        Routes.INGREDIENT_SCANNER,
        Routes.SQUAT_MASTER_ROUTE,
        Routes.REP_CHALLENGE_ROUTE,
        Routes.PUSH_UP_MASTER_ROUTE,
        Routes.PROFILE_GALLERY,
        Routes.PROFILE_DIAGNOSTICS,
        Routes.PROFILE_HELP,
        Routes.PROFILE_SETTINGS,
        Routes.PROFILE_RAISE_TICKET,
        Routes.PROFILE_MY_TICKETS,
        Routes.PROFILE_COIN_REDEMPTION,
        Routes.PROFILE_EMAIL_SUPPORT,
        Routes.PROFILE_AI_BOT
    )
    var isWorkoutSessionActive by remember { mutableStateOf(false) }

    LaunchedEffect(currentRoute) {
        if (currentRoute != Routes.WORKOUTS) {
            isWorkoutSessionActive = false
        }
    }

    val shouldHideBottomBar =
        isWorkoutSessionActive || currentRoute in bottomBarHiddenRoutes || currentRoute?.startsWith(
            Routes.OTP_VERIFICATION
        ) == true || currentRoute?.startsWith(Routes.RESET_PASSWORD) == true

    val selectedIndex = remember(currentDestination) {
        val activeRoute = tabs.firstOrNull { tab ->
            currentDestination?.hierarchy?.any { it.route == tab.route } == true
        }?.route ?: Routes.HOME
        tabs.indexOfFirst { it.route == activeRoute }.coerceAtLeast(0)
    }

    val density = LocalDensity.current
    var barWidthPx by remember { mutableFloatStateOf(0f) }

    val bumpWidthPx = with(density) { 62.dp.toPx() }
    val bumpHeightPx = with(density) { 10.dp.toPx() }
    val cornerRadiusPx = with(density) { 24.dp.toPx() }

    val tabRowInset = 28.dp
    val tabRowInsetPx = with(density) { tabRowInset.toPx() }

    val targetCenterX = if (barWidthPx > 0f) {
        val tabWidth = (barWidthPx - 2f * tabRowInsetPx) / tabs.size
        tabRowInsetPx + (selectedIndex + 0.5f) * tabWidth
    } else 0f

    val animatedCenterX by animateFloatAsState(
        targetValue = targetCenterX, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow
        ), label = "animatedCenterX"
    )

    val bumpShape =
        remember(animatedCenterX, barWidthPx, bumpWidthPx, bumpHeightPx, cornerRadiusPx) {
            BumpBarShape(
                selectedCenterXPx = animatedCenterX,
                bumpWidthPx = bumpWidthPx,
                bumpHeightPx = bumpHeightPx,
                cornerRadiusPx = cornerRadiusPx
            )
        }

    Box(
        modifier = Modifier.fillMaxSize().background(DarkBackground)
    ) {
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.navigationBars)
                .statusBarsPadding()
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(
                    onSplashFinished = {
                        val tokenStorage = appContainer.authRepository.tokenStorageManager
                        val isLoggedIn = !tokenStorage.accessToken.isNullOrBlank()
                        val destination = when {
                            isLoggedIn && tokenStorage.isProfileComplete -> Routes.HOME
                            isLoggedIn && !tokenStorage.isProfileComplete -> Routes.USER_PROFILE_SETUP
                            onboardingPreferences.isOnboardingCompleted() -> Routes.SIGN_IN
                            else -> Routes.ONBOARDING
                        }
                        navController.navigate(destination) {
                            popUpTo(Routes.SPLASH) {
                                inclusive = true
                            }
                        }
                    })
            }
            composable(Routes.ONBOARDING) {
                OnboardingScreen(
                    onOnboardingFinished = {
                        onboardingPreferences.setOnboardingCompleted(true)
                        navController.navigate(Routes.SIGN_IN) {
                            popUpTo(Routes.ONBOARDING) {
                                inclusive = true
                            }
                        }
                    })
            }
            composable(Routes.SIGN_IN) {
                SignInScreen(
                    authRepository = appContainer.authRepository,
                    onSignInSuccess = { isProfileComplete ->
                        val destination =
                            if (isProfileComplete) Routes.HOME else Routes.USER_PROFILE_SETUP
                        navController.navigate(destination) {
                            popUpTo(Routes.SIGN_IN) {
                                inclusive = true
                            }
                        }
                    },
                    onNavigateToSignUp = {
                        navController.navigate(Routes.SIGN_UP)
                    },
                    onNavigateToResetPassword = { email, token ->
                        navController.navigate("${Routes.RESET_PASSWORD}/$email/$token")
                    }
                )
            }
            composable("${Routes.RESET_PASSWORD}/{email}/{token}") { backStackEntry ->
                val userEmail = backStackEntry.savedStateHandle.get<String>("email") ?: ""
                val verificationToken = backStackEntry.savedStateHandle.get<String>("token") ?: ""
                ResetPasswordScreen(
                    userEmail = userEmail,
                    verificationToken = verificationToken,
                    authRepository = appContainer.authRepository,
                    onResetSuccess = {
                        navController.navigate(Routes.SIGN_IN) {
                            popUpTo(Routes.SIGN_IN) {
                                inclusive = true
                            }
                        }
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Routes.SIGN_UP) {
                SignUpScreen(onNavigateToOtp = { email, password ->
                    navController.navigate("${Routes.OTP_VERIFICATION}/$email/$password")
                }, onNavigateToSignIn = {
                    navController.popBackStack()
                })
            }
            composable("${Routes.OTP_VERIFICATION}/{email}/{password}") { backStackEntry ->
                val userEmail = backStackEntry.savedStateHandle.get<String>("email") ?: ""
                val userPassword = backStackEntry.savedStateHandle.get<String>("password") ?: ""
                OtpVerificationScreen(
                    userEmail = userEmail,
                    userPassword = userPassword,
                    authRepository = appContainer.authRepository,
                    onOtpVerified = {
                        val isProfileComplete =
                            appContainer.authRepository.tokenStorageManager.isProfileComplete
                        val destination =
                            if (isProfileComplete) Routes.HOME else Routes.USER_PROFILE_SETUP
                        navController.navigate(destination) {
                            popUpTo(Routes.SIGN_IN) {
                                inclusive = true
                            }
                        }
                    },
                    onBack = {
                        navController.popBackStack()
                    })
            }
            composable(Routes.USER_PROFILE_SETUP) {
                UserProfileSetupScreen(
                    authRepository = appContainer.authRepository, onSetupComplete = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.USER_PROFILE_SETUP) {
                                inclusive = true
                            }
                        }
                    })
            }
            composable(Routes.HOME) {
                HomeScreen(
                    onNavigateToDiet = { navController.navigate(Routes.DIET_HOME) },
                    onNavigateToWorkouts = { navController.navigate(Routes.WORKOUTS) },
                    onNavigateToGames = { navController.navigate(Routes.GAMES) },
                    onNavigateToCalculators = { navController.navigate(Routes.CALCULATORS) },
                    onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                    onNavigateToScanner = { navController.navigate(Routes.INGREDIENT_SCANNER) },
                    onNavigateToCoinsHub = { navController.navigate(Routes.PROFILE_COIN_REDEMPTION) })
            }
            composable(Routes.DIET_HOME) {
                DietHomeScreen(
                    mealDao = appContainer.mealDao,
                    onNavigateToScanner = { navController.navigate(Routes.INGREDIENT_SCANNER) },
                    onBack = { navController.popBackStack() })
            }
            composable(Routes.INGREDIENT_SCANNER) {
                IngredientScannerScreen(
                    mealDao = appContainer.mealDao, onBack = { navController.popBackStack() })
            }
            composable(Routes.WORKOUTS) {
                WorkoutScreen(
                    repository = appContainer.workoutRepository,
                    onActiveSessionStateChange = { isActive ->
                        isWorkoutSessionActive = isActive
                    })
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
                    authRepository = appContainer.authRepository,
                    onNavigateToSettings = { navController.navigate(Routes.PROFILE_SETTINGS) },
                    onNavigateToHelp = { navController.navigate(Routes.PROFILE_HELP) },
                    onNavigateToDiagnostics = { navController.navigate(Routes.PROFILE_DIAGNOSTICS) },
                    onNavigateToGallery = { navController.navigate(Routes.PROFILE_GALLERY) },
                    onNavigateToCoinRedemption = { navController.navigate(Routes.PROFILE_COIN_REDEMPTION) },
                    onLogout = {
                        navController.navigate(Routes.SIGN_IN) {
                            popUpTo(Routes.HOME) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
            composable(Routes.PROFILE_COIN_REDEMPTION) {
                MpCoinsHubScreen(
                    onBack = {
                        navController.popBackStack()
                    })
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
                }, onNavigateToAiBot = {
                    navController.navigate(Routes.PROFILE_AI_BOT)
                }, onNavigateToEmailSupport = {
                    navController.navigate(Routes.PROFILE_EMAIL_SUPPORT)
                }, onNavigateToRaiseTicket = {
                    navController.navigate(Routes.PROFILE_RAISE_TICKET)
                }, onNavigateToMyTickets = {
                    navController.navigate(Routes.PROFILE_MY_TICKETS)
                })
            }
            composable(Routes.PROFILE_EMAIL_SUPPORT) {
                EmailSupportScreen(
                    onBack = {
                        navController.popBackStack()
                    })
            }
            composable(Routes.PROFILE_AI_BOT) {
                AiBotSupportScreen(onBack = {
                    navController.popBackStack()
                }, onNavigateRoute = { route ->
                    navController.navigate(route)
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

        // Floating Glass App Bottom Bar
        if (!shouldHideBottomBar) {
            Box(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                    .onGloballyPositioned { barWidthPx = it.size.width.toFloat() }.clip(bumpShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xEE1A1A26), Color(0xEE12121B)
                            )
                        ), shape = bumpShape
                    ).border(
                        width = 1.dp, brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.35f),
                                OrangePrimary.copy(alpha = 0.3f),
                                Color.White.copy(alpha = 0.08f)
                            )
                        ), shape = bumpShape
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(58.dp)
                        .padding(horizontal = tabRowInset),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val selected = index == selectedIndex

                        val iconYOffset by animateDpAsState(
                            targetValue = if (selected) (-5).dp else 0.dp,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "iconY"
                        )

                        Box(
                            modifier = Modifier.weight(1f).fillMaxHeight().clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }, contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.offset(y = iconYOffset)
                            ) {
                                if (selected) {
                                    Box(
                                        modifier = Modifier.size(40.dp).clip(CircleShape)
                                            .background(
                                                brush = Brush.linearGradient(
                                                    colors = listOf(OrangePrimary, OrangeSecondary)
                                                )
                                            ), contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getTabIcon(tab.route),
                                            contentDescription = tab.title,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = getTabIcon(tab.route),
                                        contentDescription = tab.title,
                                        tint = Color.White.copy(alpha = 0.45f),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private class BumpBarShape(
    private val selectedCenterXPx: Float,
    private val bumpWidthPx: Float,
    private val bumpHeightPx: Float,
    private val cornerRadiusPx: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val path = Path().apply {
            val width = size.width
            val height = size.height
            val r = cornerRadiusPx
            val bW = bumpWidthPx
            val bH = bumpHeightPx

            val minCenter = r + bW / 2f
            val maxCenter = width - r - bW / 2f
            val cX = if (maxCenter > minCenter) {
                selectedCenterXPx.coerceIn(minCenter, maxCenter)
            } else {
                width / 2f
            }

            val leftX = cX - bW / 2f
            val rightX = cX + bW / 2f

            moveTo(r, 0f)

            lineTo(leftX, 0f)

            cubicTo(
                leftX + (rightX - leftX) * 0.25f, 0f, leftX + (rightX - leftX) * 0.25f, -bH, cX, -bH
            )
            cubicTo(
                rightX - (rightX - leftX) * 0.25f,
                -bH,
                rightX - (rightX - leftX) * 0.25f,
                0f,
                rightX,
                0f
            )

            lineTo(width - r, 0f)

            arcTo(
                rect = Rect(width - 2 * r, 0f, width, 2 * r),
                startAngleDegrees = -90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            lineTo(width, height - r)

            arcTo(
                rect = Rect(width - 2 * r, height - 2 * r, width, height),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            lineTo(r, height)

            arcTo(
                rect = Rect(0f, height - 2 * r, 2 * r, height),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            lineTo(0f, r)

            arcTo(
                rect = Rect(0f, 0f, 2 * r, 2 * r),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            close()
        }
        return Outline.Generic(path)
    }
}
