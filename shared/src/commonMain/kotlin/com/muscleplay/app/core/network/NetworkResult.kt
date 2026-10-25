package com.muscleplay.app.core.network

sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(val statusCode: Int, val message: String) : NetworkResult<Nothing>()
    data class Exception(val throwable: Throwable) : NetworkResult<Nothing>()

    fun <R> map(transform: (T) -> R): NetworkResult<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> Error(statusCode, message)
            is Exception -> Exception(throwable)
        }
    }

    inline fun onSuccess(action: (T) -> Unit): NetworkResult<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (code: Int, message: String) -> Unit): NetworkResult<T> {
        if (this is Error) action(statusCode, message)
        return this
    }

    inline fun onException(action: (Throwable) -> Unit): NetworkResult<T> {
        if (this is Exception) action(throwable)
        return this
    }
}
