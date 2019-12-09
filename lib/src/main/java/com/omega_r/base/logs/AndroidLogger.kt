package com.omega_r.base.logs

import android.util.Log
import com.omega_r.base.logs.Logger.Level.*

/**
 * Created by Anton Knyazev on 03.12.2019.
 */
class AndroidLogger: Logger {

    override fun log(
        level: Logger.Level,
        tag: String,
        throwable: Throwable?,
        message: String?
    ) {
        when (level) {
            DEBUG -> Log.d(tag, message, throwable)
            INFO -> Log.i(tag, message, throwable)
            WARNING -> Log.w(tag, message, throwable)
            ERROR -> Log.e(tag, message, throwable)
        }
    }


}