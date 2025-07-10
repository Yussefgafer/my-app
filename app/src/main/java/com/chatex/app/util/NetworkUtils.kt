package com.chatex.app.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retryWhen
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

/**
 * فئة مساعدة للتعامل مع طلبات الشبكة مع إعادة المحاولة
 */
object NetworkUtils {

    /**
     * تنفيذ عملية مع إعادة المحاولة التلقائية
     * @param maxRetries عدد مرات إعادة المحاولة القصوى
     * @param initialDelay التأخير الأولي قبل إعادة المحاولة (بالميلي ثانية)
     * @param maxDelay أقصى تأخير بين المحاولات (بالميلي ثانية)
     * @param factor معامل زيادة التأخير بين المحاولات
     * @param shouldRetry دالة لتحديد ما إذا كان يجب إعادة المحاولة في حالة حدوث خطأ معين
     */
    fun <T> withRetry(
        maxRetries: Int = 3,
        initialDelay: Long = 1000, // 1 ثانية
        maxDelay: Long = 30000,    // 30 ثانية
        factor: Double = 2.0,
        shouldRetry: (Throwable) -> Boolean = ::isNetworkError,
        block: suspend (attempt: Int) -> T
    ): Flow<Result<T>> = flow {
        var currentDelay = initialDelay
        var retryCount = 0
        var lastError: Throwable? = null

        while (retryCount <= maxRetries) {
            try {
                val result = block(retryCount)
                emit(Result.success(result))
                return@flow
            } catch (e: Exception) {
                lastError = e
                
                if (!shouldRetry(e) || retryCount == maxRetries) {
                    emit(Result.failure(e))
                    return@flow
                }

                // زيادة عدد مرات إعادة المحاولة
                retryCount++
                
                // حساب التأخير التالي مع التزايد الأسي
                val nextDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
                
                // إرسال حالة إعادة المحاولة
                emit(Result.failure(RetryableException(e, retryCount, nextDelay, maxRetries)))
                
                // الانتظار قبل إعادة المحاولة
                delay(nextDelay)
                currentDelay = nextDelay
            }
        }

        // في حالة فشل كل المحاولات
        emit(Result.failure(lastError ?: IllegalStateException("فشل غير معروف")))
    }

    /**
     * تحديد ما إذا كان الخطأ ناتجًا عن مشكلة في الشبكة
     */
    fun isNetworkError(throwable: Throwable): Boolean {
        return when (throwable) {
            is SocketTimeoutException,
            is UnknownHostException,
            is SSLHandshakeException,
            is HttpException -> true
            
            is IOException -> throwable.message?.contains("Network is unreachable") == true ||
                             throwable.message?.contains("timeout") == true ||
                             throwable.message?.contains("failed to connect") == true
            
            else -> false
        }
    }

    /**
     * تمديد لـ Flow لإضافة إعادة المحاولة التلقائية
     */
    fun <T> Flow<T>.withRetry(
        maxRetries: Int = 3,
        initialDelay: Long = 1000,
        maxDelay: Long = 30000,
        factor: Double = 2.0,
        shouldRetry: (Throwable) -> Boolean = ::isNetworkError
    ): Flow<T> = this.retryWhen { cause, attempt ->
        if (!shouldRetry(cause) || attempt >= maxRetries) {
            return@retryWhen false
        }
        
        val delayTime = (initialDelay * Math.pow(factor, attempt.toDouble())).toLong().coerceAtMost(maxDelay)
        delay(delayTime)
        true
    }
}

/**
 * استثناء يشير إلى إمكانية إعادة المحاولة
 * @property originalException الاستثناء الأصلي
 * @property retryCount عدد مرات إعادة المحاولة الحالية
 * @property nextDelay التأخير قبل المحاولة التالية (بالميلي ثانية)
 * @property maxRetries الحد الأقصى لعدد مرات إعادة المحاولة
 */
class RetryableException(
    val originalException: Throwable,
    val retryCount: Int,
    val nextDelay: Long,
    val maxRetries: Int
) : Exception("محاولة $retryCount من $maxRetries - سيتم إعادة المحاولة بعد ${nextDelay}ms", originalException)
