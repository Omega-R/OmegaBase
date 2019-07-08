package com.omega_r.base.launchers

import android.content.Context
import android.content.Intent

/**
 * Created by Anton Knyazev on 2019-07-08.
 */
class IntentLauncher(private val intent: Intent) : Launcher {

    override fun launch(context: Context) {
        context.startActivity(intent)
    }

}