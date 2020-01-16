package com.omega_r.base.errors

import kotlinx.coroutines.CoroutineExceptionHandler
import retrofit2.HttpException
import java.net.ConnectException
import java.net.HttpURLConnection.*
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.CoroutineContext

/**
 * Created by Anton Knyazev on 2019-05-28.
 */
open class ErrorHandler : (Throwable) -> Exception, CoroutineExceptionHandler {

    override val key: CoroutineContext.Key<*>
        get() = CoroutineExceptionHandler

    protected open fun handleHttpException(httpException: HttpException): AppException {
        val response = httpException.response()

        when (response?.code()) {
            HTTP_INTERNAL_ERROR -> return AppException.ServerProblem(
                "Internal server error",
                httpException
            )
            HTTP_BAD_REQUEST -> return AppException.ServerProblem("Bad request", httpException)
            HTTP_UNAVAILABLE -> return AppException.ServerProblem("Service Unavailable", httpException)
            HTTP_NOT_FOUND -> return AppException.NotFound("Not found", httpException)
            HTTP_FORBIDDEN -> return AppException.AccessDenied("Forbidden", httpException)
            HTTP_UNAUTHORIZED-> return AppException.NotAuthorized("Unauthorized", httpException)
        }

        return AppException.UnknownError("Unknown error type", httpException)
    }

    open fun handleThrowable(throwable: Throwable): AppException {
        return when (throwable) {
            is UnknownHostException -> AppException.NoConnection(null, throwable)
            is ConnectException,
            is SocketTimeoutException -> AppException.ServerUnavailable(null, throwable)
            is HttpException -> handleHttpException(throwable)
            is AppException -> throwable
            else -> AppException.UnknownError("Unknown error", throwable)
        }
    }

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        throw handleThrowable(exception)
    }

    override operator fun invoke(it: Throwable): AppException {
        return handleThrowable(it)
    }

}