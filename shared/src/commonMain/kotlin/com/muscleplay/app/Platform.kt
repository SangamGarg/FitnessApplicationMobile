package com.muscleplay.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform