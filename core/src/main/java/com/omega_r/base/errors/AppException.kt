package com.omega_r.base.errors

import com.omega_r.libs.omegatypes.Text

/**
 * Created by Anton Knyazev on 2019-05-28.
 */

private const val NO_MESSAGE = " "

open class AppException(
    devMessage: String?,
    val userMessage: Text? = null,
    cause: Throwable? = null,
) : Exception(devMessage ?: cause?.toString() ?: NO_MESSAGE) {

    init {
        cause?.let {
            initCause(it)
        }
    }

    class NotImplemented(devMessage: String?, userMessage: Text? = null, cause: Throwable? = null) :
        AppException(devMessage, userMessage, cause)

    class NoConnection(devMessage: String?, userMessage: Text? = null, cause: Throwable? = null) :
        AppException(devMessage, userMessage, cause)

    class ServerUnavailable(devMessage: String?, userMessage: Text? = null, cause: Throwable? = null) :
        AppException(devMessage, userMessage, cause)

    class ServerProblem(devMessage: String?, userMessage: Text? = null, cause: Throwable? = null) :
        AppException(devMessage, userMessage, cause)

    class NotFound(devMessage: String?, userMessage: Text? = null, cause: Throwable? = null) :
        AppException(devMessage, userMessage, cause)

    class AccessDenied(devMessage: String?, userMessage: Text? = null, cause: Throwable? = null) :
        AppException(devMessage, userMessage, cause)

    class NotAuthorized(devMessage: String?, userMessage: Text? = null, cause: Throwable? = null) :
        AppException(devMessage, userMessage, cause)

    class AuthorizedFailed(devMessage: String?, userMessage: Text? = null, cause: Throwable? = null) :
        AppException(devMessage, userMessage, cause)

    class NoData(devMessage: String?, userMessage: Text? = null, cause: Throwable? = null) :
        AppException(devMessage, userMessage, cause)

    class UnknownError(devMessage: String?, userMessage: Text? = null, cause: Throwable? = null) :
        AppException(devMessage, userMessage, cause)
}

@Suppress("NOTHING_TO_INLINE")
inline fun throwNoData(devMessage: String? = null): Nothing = throw AppException.NoData(devMessage)