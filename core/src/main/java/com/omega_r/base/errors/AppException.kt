package com.omega_r.base.errors

/**
 * Created by Anton Knyazev on 2019-05-28.
 */

private const val NO_MESSAGE = " "

open class AppException(
    devMessage: String?,
    cause: Throwable? = null
) : Exception(devMessage ?: cause?.toString() ?: NO_MESSAGE) {

    init {
        cause?.let {
            initCause(it)
        }
    }

    class NoConnection(devMessage: String?, cause: Throwable? = null) : AppException(devMessage, cause)

    class ServerUnavailable(devMessage: String?, cause: Throwable? = null) : AppException(devMessage, cause)

    class ServerProblem(devMessage: String?, cause: Throwable? = null) : AppException(devMessage, cause)

    class NotFound(devMessage: String?, cause: Throwable? = null) : AppException(devMessage, cause)

    class AccessDenied(devMessage: String?, cause: Throwable? = null) : AppException(devMessage, cause)

    class NotAuthorized(devMessage: String?, cause: Throwable? = null) : AppException(devMessage, cause)

    class AuthorizedFailed(devMessage: String?, cause: Throwable? = null) : AppException(devMessage, cause)

    class NoData(devMessage: String?, cause: Throwable? = null) : AppException(devMessage, cause)

    class UnknownError(devMessage: String?, cause: Throwable? = null) : AppException(devMessage, cause)

}

inline fun throwNoData(message: String? = null): Nothing = throw AppException.NoData(message)