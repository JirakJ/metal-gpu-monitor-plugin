package dev.htmeta.mgm.model

sealed class NativeBridgeResult<out T> {
    data class Success<T>(val data: T) : NativeBridgeResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : NativeBridgeResult<Nothing>()
    data object Unavailable : NativeBridgeResult<Nothing>()

    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    fun getOrDefault(default: @UnsafeVariance T): T = when (this) {
        is Success -> data
        else -> default
    }

    fun <R> map(transform: (T) -> R): NativeBridgeResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> Error(message, cause)
        is Unavailable -> Unavailable
    }
}
