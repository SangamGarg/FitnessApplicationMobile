package com.muscleplay.app.core.network.error

sealed interface NetworkError {
    data object NoInternet : NetworkError
    data object Timeout : NetworkError
    data class Http(
        val statusCode: Int,
        val message: String?,
    ) : NetworkError

    data class Serialization(
        val message: String?,
    ) : NetworkError

    data class Unknown(
        val message: String?,
    ) : NetworkError
}
