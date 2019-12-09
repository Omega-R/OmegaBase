package com.omega_r.base.logs

import androidx.collection.ArraySet
import com.omega_r.base.logs.Logger.Level
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by Anton Knyazev on 2019-12-02.
 */
object LogManager {

    private val loggersMap: MutableMap<Level, MutableSet<Logger>> = ConcurrentHashMap()

    fun isEmpty(level: Level) = loggersMap[level].isNullOrEmpty()

    fun addLogger(logger: Logger, levels: Array<Level> = Level.values()) = apply {
        levels.forEach { level: Level ->
            loggersMap
                .getOrPut(level) { ArraySet() }
                .add(logger)
        }
    }

    fun log(
        level: Level = Level.DEBUG,
        tag: String,
        throwable: Throwable? = null,
        message: String?
    ) {
        loggersMap[level]?.forEach {
            it.log(level, tag, throwable, message)
        }
    }

}

inline fun <reified T> T.log(
    level: Level = Level.DEBUG,
    tag: String = T::class.java.simpleName,
    throwable: Throwable? = null,
    messageBlock: () -> String
) {
    if (!LogManager.isEmpty(level)) {
        LogManager.log(
            level = level,
            tag = tag,
            throwable = throwable,
            message = messageBlock()
        )
    }
}

inline fun <reified T> T.log(
    throwable: Throwable,
    level: Level = Level.WARNING,
    tag: String = T::class.java.simpleName
) {
    if (!LogManager.isEmpty(level)) {
        LogManager.log(
            level = level,
            tag = tag,
            throwable = throwable,
            message = null
        )
    }
}