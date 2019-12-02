package com.omega_r.base.logs

import java.util.concurrent.CopyOnWriteArrayList

/**
 * Created by Anton Knyazev on 2019-12-02.
 */
class LogManager {

    val logs: List<Log> = CopyOnWriteArrayList()

    fun log(level: Log.Level, message: String, throwable: Throwable?) {
        logs.forEach { it.log(level, message, throwable) }
    }


}