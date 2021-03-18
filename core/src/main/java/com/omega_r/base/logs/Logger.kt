package com.omega_r.base.logs

/**
 * Created by Anton Knyazev on 2019-12-02.
 */
interface Logger {

    fun log(level: Level, tag: String, throwable: Throwable?, message: String?)

    enum class Level {

        DEBUG, INFO, WARNING, ERROR

    }

}
