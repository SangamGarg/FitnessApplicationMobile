package com.muscleplay.app.core.network

object SimpleJsonParser {
    fun parseErrorMessage(json: String, fallback: String = "An unexpected error occurred"): String {
        if (json.isBlank()) return fallback
        val message = parseString(json, "message") ?: parseString(json, "error") ?: parseString(json, "status")
        return message?.takeIf { it.isNotBlank() } ?: fallback
    }

    fun parseString(json: String, key: String): String? {
        val pattern = "\"$key\"\\s*:\\s*\"([^\"]*)\"".toRegex()
        return pattern.find(json)?.groupValues?.get(1)
    }

    fun parseInt(json: String, key: String): Int? {
        val pattern = "\"$key\"\\s*:\\s*([0-9]+)".toRegex()
        return pattern.find(json)?.groupValues?.get(1)?.toIntOrNull()
    }

    fun parseLong(json: String, key: String): Long? {
        val pattern = "\"$key\"\\s*:\\s*([0-9]+)".toRegex()
        return pattern.find(json)?.groupValues?.get(1)?.toLongOrNull()
    }

    fun parseBoolean(json: String, key: String): Boolean {
        val pattern = "\"$key\"\\s*:\\s*(true|false)".toRegex()
        return pattern.find(json)?.groupValues?.get(1)?.toBoolean() ?: false
    }

    fun parseFloat(json: String, key: String): Float? {
        val pattern = "\"$key\"\\s*:\\s*([0-9]+\\.?[0-9]*)".toRegex()
        return pattern.find(json)?.groupValues?.get(1)?.toFloatOrNull()
    }
}
