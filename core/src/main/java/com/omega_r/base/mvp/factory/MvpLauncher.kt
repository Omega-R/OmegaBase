package com.omega_r.base.mvp.factory

import android.os.SystemClock
import com.omegar.libs.omegalaunchers.Launcher

interface MvpLauncher : Launcher {

    fun generateUniqueKey() = SystemClock.elapsedRealtime().toInt()

    fun preparePresenter(): Boolean
}