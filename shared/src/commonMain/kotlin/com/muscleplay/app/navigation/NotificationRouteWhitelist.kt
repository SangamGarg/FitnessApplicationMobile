package com.muscleplay.app.navigation

object NotificationRouteWhitelist {
    private val allowedRoutes = mapOf(
        "home" to Routes.HOME,
        "workouts" to Routes.WORKOUTS,
        "games" to Routes.GAMES,
        "calculators" to Routes.CALCULATORS,
        "profile" to Routes.PROFILE,
        "profile_coin_redemption" to Routes.PROFILE_COIN_REDEMPTION,
        "profile_my_tickets" to Routes.PROFILE_MY_TICKETS,
        "profile_help" to Routes.PROFILE_HELP,
        "profile_settings" to Routes.PROFILE_SETTINGS,
        "diet_home" to Routes.DIET_HOME,
        "notifications" to Routes.NOTIFICATIONS
    )

    fun mapToSafeRoute(rawRoute: String?): String? {
        if (rawRoute.isNullOrBlank()) return null
        val cleaned = rawRoute.lowercase().trim().removePrefix("/")
        return allowedRoutes[cleaned]
    }
}
