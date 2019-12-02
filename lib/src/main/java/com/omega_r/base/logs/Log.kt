package com.omega_r.base.logs

/**
 * Created by Anton Knyazev on 2019-12-02.
 */
interface Log {

    fun log(level: Level, message: String, throwable: Throwable? = null)

    enum class Level {

        DEBUG, INFO, WARNING, ERROR

    }

}