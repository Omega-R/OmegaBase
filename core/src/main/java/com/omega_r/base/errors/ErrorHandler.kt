package com.omega_r.base.errors

import kotlinx.coroutines.CoroutineExceptionHandler
import retrofit2.HttpException
import retrofit2.Invocation
import java.net.ConnectException
import java.net.HttpURLConnection.HTTP_BAD_REQUEST
import java.net.HttpURLConnection.HTTP_FORBIDDEN
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import java.net.HttpURLConnection.HTTP_NOT_FOUND
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import java.net.HttpURLConnection.HTTP_UNAVAILABLE
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.CoroutineContext

/**
 * Created by Anton Knyazev on 2019-05-28.
 */
open class ErrorHandler : (Throwable) -> Exception, CoroutineExceptionHandler {

    override val key: CoroutineContext.Key<*>
        get() = CoroutineExceptionHandler

    protected open fun handleHttpException(httpException: HttpException, method: String?): AppException {
        val response = httpException.response()

        when (response?.code()) {
            HTTP_INTERNAL_ERROR -> return AppException.ServerProblem(
                devMessage = "Internal server error\n$method",
                cause = httpException
            )
            HTTP_BAD_REQUEST -> return AppException.ServerProblem(
                devMessage = "Bad request\n$method",
                cause = httpException
            )
            HTTP_UNAVAILABLE -> return AppException.ServerProblem(
                devMessage = "Service Unavailable\n$method",
                cause = httpException
            )
            HTTP_NOT_FOUND -> return AppException.NotFound(
                devMessage = "Not found\n$method",
                cause = httpException
            )
            HTTP_FORBIDDEN -> return AppException.AccessDenied(
                devMessage = "Forbidden\n$method",
                cause = httpException
            )
            HTTP_UNAUTHORIZED -> return AppException.NotAuthorized(
                devMessage = "Unauthorized\n$method",
                cause = httpException
            )
        }

        return AppException.UnknownError(
            devMessage = "Unknown error type",
            cause = httpException
        )
    }

    open fun handleThrowable(throwable: Throwable): AppException {
        return when (throwable) {
            is UnknownHostException -> AppException.NoConnection(
                devMessage = null,
                cause = throwable
            )
            is ConnectException,
            is SocketTimeoutException,
            -> AppException.ServerUnavailable(
                devMessage = null,
                cause = throwable
            )
            is HttpException -> {
                handleHttpException(throwable, throwable.getMethod())
            }
            is AppException -> throwable
            else -> AppException.UnknownError(
                devMessage = "Unknown error",
                cause = throwable
            )
        }
    }

    protected open fun HttpException.getMethod(): String? {
        return response()?.raw()?.request()?.let {
            var result = "@" + it.method() + "(" + it.url().url().toString() + ") "

            it.tag(Invocation::class.java)?.let { tag ->
                val arguments = tag.arguments().joinToString()
                result += tag.method().declaringClass.simpleName + "." + tag.method().name + "(" + arguments + ")"
            }

            result
        }
    }

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        throw handleThrowable(exception)
    }

    override operator fun invoke(it: Throwable): AppException {
        return handleThrowable(it)
    }
}