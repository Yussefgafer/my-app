package com.chatex.app.util

/**
 * A sealed class that encapsulates successful outcome with a value of type [T]
 * or a failure with message and throwable.
 */
sealed class NetworkResult<out T> {
    /**
     * Represents a successful network operation with a result of type [T]
     */
    data class Success<out T>(val data: T) : NetworkResult<T>() {
        override fun isSuccess() = true
        override fun getOrNull() = data
    }

    /**
     * Represents a failed network operation with an error message and optional throwable
     */
    data class Error(
        val message: String,
        val throwable: Throwable? = null,
        val code: Int? = null
    ) : NetworkResult<Nothing>() {
        constructor(throwable: Throwable) : this(throwable.message ?: "Unknown error", throwable)
        
        override fun isSuccess() = false
        override fun getOrNull(): Nothing? = null
    }

    /**
     * Represents a loading state when the network operation is in progress
     */
    object Loading : NetworkResult<Nothing>() {
        override fun isSuccess() = false
        override fun getOrNull(): Nothing? = null
    }

    /**
     * Returns true if the result is a success
     */
    abstract fun isSuccess(): Boolean

    /**
     * Returns the encapsulated result if this instance represents success or null if it is failure
     */
    abstract fun getOrNull(): T?

    /**
     * Returns the encapsulated result if this instance represents success or the result of [defaultValue]
     * function for the encapsulated Throwable exception if it is failure.
     */
    inline fun getOrElse(defaultValue: (Throwable) -> T): T {
        return when (this) {
            is Success -> data
            is Error -> defaultValue(throwable ?: Exception(message))
            is Loading -> defaultValue(Exception("Loading in progress"))
        }
    }

    /**
     * Returns the encapsulated result if this instance represents success or the [defaultValue] if it is failure
     */
    fun getOrDefault(defaultValue: T): T {
        return getOrElse { defaultValue }
    }

    /**
     * Returns the encapsulated result if this instance represents success or null if it is failure
     */
    fun getOrNull(): T? = getOrNull()

    /**
     * Returns the encapsulated result if this instance represents success or throws an exception if it is failure
     */
    fun getOrThrow(): T {
        return when (this) {
            is Success -> data
            is Error -> throw throwable ?: Exception(message)
            is Loading -> throw Exception("Loading in progress")
        }
    }

    /**
     * Maps the result using the given [transform] function if this instance represents success
     */
    inline fun <R> map(transform: (T) -> R): NetworkResult<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> this
            is Loading -> this
        }
    }

    /**
     * Maps the error using the given [transform] function if this instance represents failure
     */
    fun mapError(transform: (Error) -> Error): NetworkResult<T> {
        return when (this) {
            is Success -> this
            is Error -> transform(this)
            is Loading -> this
        }
    }

    /**
     * Executes the given [action] if this instance represents success
     */
    inline fun onSuccess(action: (T) -> Unit): NetworkResult<T> {
        if (this is Success) action(data)
        return this
    }

    /**
     * Executes the given [action] if this instance represents failure
     */
    inline fun onError(action: (Error) -> Unit): NetworkResult<T> {
        if (this is Error) action(this)
        return this
    }

    /**
     * Executes the given [action] if this instance represents loading state
     */
    inline fun onLoading(action: () -> Unit): NetworkResult<T> {
        if (this is Loading) action()
        return this
    }

    companion object {
        /**
         * Creates a [NetworkResult.Success] with the given [data]
         */
        fun <T> success(data: T): NetworkResult<T> = Success(data)

        /**
         * Creates a [NetworkResult.Error] with the given [message] and optional [throwable]
         */
        fun <T> error(message: String, throwable: Throwable? = null): NetworkResult<T> {
            return Error(message, throwable)
        }

        /**
         * Creates a [NetworkResult.Error] from the given [throwable]
         */
        fun <T> error(throwable: Throwable): NetworkResult<T> {
            return Error(throwable.message ?: "Unknown error", throwable)
        }

        /**
         * Creates a [NetworkResult.Loading] instance
         */
        fun <T> loading(): NetworkResult<T> = Loading
    }
}
