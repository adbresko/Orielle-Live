package com.orielle.domain.model

/**
 * A generic sealed class that represents the state of a data operation.
 * It can be Loading, Success, or Failure. This is a standard pattern for
 * handling asynchronous responses in a clean, type-safe way.
 *
 * @param T The type of the data being handled.
 */
sealed class Response<out T> {
    /**
     * Represents a loading state, typically shown before the result is available.
     */
    object Loading : Response<Nothing>()

    /**
     * Represents a successful operation.
     * @param data The data returned from the successful operation.
     */
    data class Success<out T>(
        val data: T
    ) : Response<T>()

    /**
     * Represents a failed operation.
     * @param exception The exception that occurred during the operation.
     */
    data class Failure(
        val exception: Exception
    ) : Response<Nothing>()
}
