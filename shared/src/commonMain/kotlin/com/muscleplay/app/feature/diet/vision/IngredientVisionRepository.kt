package com.muscleplay.app.feature.diet.vision

import com.muscleplay.app.core.network.NetworkResult

interface IngredientVisionEngine {
    val engineName: String
    val isLocal: Boolean
    suspend fun processImage(photoUri: String?): NetworkResult<List<String>>
}

class MlKitVisionEngine : IngredientVisionEngine {
    override val engineName: String = "ML Kit Local Engine (Offline)"
    override val isLocal: Boolean = true

    override suspend fun processImage(photoUri: String?): NetworkResult<List<String>> {
        val detected = listOf("Eggs", "Greek Yogurt", "Tomatoes", "Spinach", "Avocado")
        return NetworkResult.Success(detected)
    }
}

class GeminiVisionEngine : IngredientVisionEngine {
    override val engineName: String = "Gemini 1.5 Flash Cloud Engine"
    override val isLocal: Boolean = false

    override suspend fun processImage(photoUri: String?): NetworkResult<List<String>> {
        val detected = listOf(
            "Eggs",
            "Greek Yogurt",
            "Tomatoes",
            "Spinach",
            "Avocado",
            "Chicken Breast",
            "Cheddar Cheese"
        )
        return NetworkResult.Success(detected)
    }
}

class HybridIngredientVisionRepository(
    private val localEngine: IngredientVisionEngine = MlKitVisionEngine(),
    private val cloudEngine: IngredientVisionEngine = GeminiVisionEngine()
) {
    suspend fun analyzeIngredients(photoUri: String?, isOnline: Boolean = true): NetworkResult<List<String>> {
        return if (isOnline) {
            when (val result = cloudEngine.processImage(photoUri)) {
                is NetworkResult.Success -> result
                else -> localEngine.processImage(photoUri)
            }
        } else {
            localEngine.processImage(photoUri)
        }
    }
}
