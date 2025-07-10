package com.chatex.app.util

import android.content.Context
import com.chatex.app.R
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.net.ssl.SSLHandshakeException

/**
 * Handles errors and provides user-friendly error messages
 */
class ErrorHandler @Inject constructor(
    private val context: Context
) {
    /**
     * Get a user-friendly error message for an exception
     */
    fun getErrorMessage(throwable: Throwable?): String {
        return when (throwable) {
            is ConnectException -> context.getString(R.string.error_connection_failed)
            is SocketTimeoutException -> context.getString(R.string.error_connection_timeout)
            is UnknownHostException -> context.getString(R.string.error_no_internet)
            is SSLHandshakeException -> context.getString(R.string.error_ssl_handshake)
            is IOException -> context.getString(R.string.error_network_io)
            is IllegalArgumentException -> throwable.message ?: context.getString(R.string.error_invalid_input)
            is IllegalStateException -> throwable.message ?: context.getString(R.string.error_illegal_state)
            else -> throwable?.message ?: context.getString(R.string.error_unknown)
        }
    }

    /**
     * Check if an error is a network error that can be retried
     */
    fun isNetworkError(throwable: Throwable): Boolean {
        return throwable is IOException ||
                throwable is SocketTimeoutException ||
                throwable is UnknownHostException ||
                throwable is SSLHandshakeException
    }
}
