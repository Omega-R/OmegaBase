package com.omega_r.base.logs

import com.omega_r.libs.extensions.log.log
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Created by Anton Knyazev on 2019-12-02.
 */
object LogManager {

    private val loggers: List<Logger> = CopyOnWriteArrayList()

    fun isEmpty() = loggers.isEmpty()

    fun log(
        level: Logger.Level = Logger.Level.DEBUG,
        throwable: Throwable? = null,
        tag: String,
        message: String
    ) {
        loggers.forEach { it.log(level, tag, message, throwable) }
    }

    inline fun <reified T> T.log(
        level: Logger.Level = Logger.Level.DEBUG,
        throwable: Throwable? = null,
        tag: String = T::class.java.simpleName,
        messageBlock: () -> String
    ) {
        if (!isEmpty()) {
            log(level, throwable, tag, messageBlock())
        }
    }

}