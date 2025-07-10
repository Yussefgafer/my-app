package com.chatex.app.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * A generic network bound resource that provides a resource from local database as the source of
 * truth and creates the data on remote server if needed. It serves the data from local database
 * first, loads the data from network and updates the local database.
 *
 * @param ResultType The type of the resource data.
 * @param RequestType The type of the API response.
 *
 * @param query A function to get the data from local database.
 * @param fetch A function to fetch the data from network.
 * @param saveFetchResult A function to save the result of the API response into the local database.
 * @param shouldFetch A function to decide whether to fetch data from network or not.
 * @param onFetchFailed A function to handle the error when fetch fails.
 * @return A flow of [NetworkResult] that emits the resource data.
 */
fun <ResultType, RequestType> networkBoundResource(
    query: () -> Flow<ResultType>,
    fetch: suspend () -> NetworkResult<RequestType>,
    saveFetchResult: suspend (RequestType) -> Unit,
    shouldFetch: (ResultType?) -> Boolean = { true },
    onFetchFailed: (Throwable?) -> Unit = {}
): Flow<NetworkResult<ResultType>> = flow {
    // Emit loading state
    emit(NetworkResult.Loading)

    // First check if we should fetch data from network
    val data = query().map { it }.first()
    
    if (shouldFetch(data)) {
        // Emit loading state with cached data if available
        if (data != null) {
            emit(NetworkResult.Success(data))
        }

        try {
            // Fetch from network
            val response = fetch()
            
            when (response) {
                is NetworkResult.Success -> {
                    // Save the result to local database
                    saveFetchResult(response.data)
                    // Emit the latest data from local database
                    emitAll(query().map { NetworkResult.Success(it) })
                }
                is NetworkResult.Error -> {
                    onFetchFailed(response.throwable)
                    // If we have cached data, emit it, otherwise emit the error
                    if (data != null) {
                        emit(NetworkResult.Success(data))
                    } else {
                        emit(NetworkResult.Error(response.message, response.throwable))
                    }
                }
                is NetworkResult.Loading -> {
                    // Do nothing, already in loading state
                }
            }
        } catch (e: Exception) {
            onFetchFailed(e)
            // If we have cached data, emit it, otherwise emit the error
            if (data != null) {
                emit(NetworkResult.Success(data))
            } else {
                emit(NetworkResult.Error(e.message ?: "Unknown error", e))
            }
        }
    } else {
        // Emit the data from local database
        emitAll(query().map { NetworkResult.Success(it) })
    }
}

/**
 * A simpler version of networkBoundResource that doesn't require mapping between API and database types
 */
fun <T> networkBoundResource(
    query: () -> Flow<T>,
    fetch: suspend () -> NetworkResult<T>,
    saveFetchResult: suspend (T) -> Unit,
    shouldFetch: (T?) -> Boolean = { true },
    onFetchFailed: (Throwable?) -> Unit = {}
): Flow<NetworkResult<T>> = networkBoundResource(
    query = query,
    fetch = fetch,
    saveFetchResult = saveFetchResult,
    shouldFetch = shouldFetch,
    onFetchFailed = onFetchFailed
)

/**
 * Extension function to convert a flow to a NetworkResult flow
 */
fun <T> Flow<T>.asNetworkResult(
    onError: (Throwable) -> Unit = {}
): Flow<NetworkResult<T>> = flow {
    try {
        emit(NetworkResult.Loading)
        collect {
            emit(NetworkResult.Success(it))
        }
    } catch (e: Exception) {
        onError(e)
        emit(NetworkResult.Error(e.message ?: "An error occurred", e))
    }
}

/**
 * Extension function to handle network result with success and error callbacks
 */
suspend fun <T> NetworkResult<T>.onResult(
    onSuccess: suspend (T) -> Unit = {},
    onError: suspend (String, Throwable?) -> Unit = { _, _ -> },
    onLoading: suspend () -> Unit = {}
) {
    when (this) {
        is NetworkResult.Success -> onSuccess(data)
        is NetworkResult.Error -> onError(message, throwable)
        is NetworkResult.Loading -> onLoading()
    }
}

/**
 * Extension function to transform a NetworkResult of one type to another
 */
fun <T, R> NetworkResult<T>.mapResult(
    transform: (T) -> R
): NetworkResult<R> {
    return when (this) {
        is NetworkResult.Success -> NetworkResult.Success(transform(data))
        is NetworkResult.Error -> this
        is NetworkResult.Loading -> this
    }
}

/**
 * Extension function to transform a NetworkResult of one type to another with suspend function
 */
suspend fun <T, R> NetworkResult<T>.mapResultSuspend(
    transform: suspend (T) -> R
): NetworkResult<R> {
    return when (this) {
        is NetworkResult.Success -> NetworkResult.Success(transform(data))
        is NetworkResult.Error -> this
        is NetworkResult.Loading -> this
    }
}

/**
 * Extension function to handle error cases in NetworkResult
 */
suspend fun <T> NetworkResult<T>.onError(
    action: suspend (String, Throwable?) -> Unit
): NetworkResult<T> {
    if (this is NetworkResult.Error) {
        action(message, throwable)
    }
    return this
}

/**
 * Extension function to handle success cases in NetworkResult
 */
suspend fun <T> NetworkResult<T>.onSuccess(
    action: suspend (T) -> Unit
): NetworkResult<T> {
    if (this is NetworkResult.Success) {
        action(data)
    }
    return this
}
