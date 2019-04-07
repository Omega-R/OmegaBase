package com.omega_r.base.launchers

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.AndroidRuntimeException
import androidx.fragment.app.Fragment
import com.omega_r.base.tools.BundlePair
import com.omega_r.base.tools.bundleOf
import java.io.Serializable

/**
 * Created by Anton Knyazev on 06.04.2019.
 */
data class ActivityLauncher(private val activityClass: Class<Activity>,
                                           private val bundle: Bundle? = null,
                                           private var flags: Int = 0) : Launcher, Serializable {

    constructor(activityClass: Class<Activity>, vararg extraParams: BundlePair, flags: Int = 0)
            : this(activityClass, bundleOf(*extraParams), flags)

    private fun createIntent(context: Context): Intent {
        return Intent(context, activityClass).apply {
            if (bundle != null) {
                putExtras(bundle)
            }
            flags = this@ActivityLauncher.flags
        }
    }

    fun addFlags(flag: Int): Launcher {
        flags = flags or flag
        return this
    }

    fun removeFlags(flag: Int): Launcher {
        flags = flags and (flag.inv())
        return this
    }

    fun launch(context: Context, option: Bundle? = null) {
        val intent = createIntent(context)
        try {
            context.compatStartActivity(intent, option)
        } catch (exc: AndroidRuntimeException) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.compatStartActivity(intent, option)
        }
    }

    private fun Context.compatStartActivity(intent: Intent,  option: Bundle? = null) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            startActivity(intent)
        } else {
            startActivity(intent, option)
        }
    }

    private fun Activity.compatStartActivityForResult(intent: Intent, requestCode: Int, option: Bundle? = null) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            startActivityForResult(intent, requestCode)
        } else {
            startActivityForResult(intent, requestCode, option)
        }
    }

    fun getPendingIntent(context: Context, requestCode: Int = 0,
                         flags: Int = PendingIntent.FLAG_UPDATE_CURRENT): PendingIntent {
        return PendingIntent.getActivity(context, requestCode, createIntent(context), flags)
    }

    fun launchForResult(activity: Activity, requestCode: Int, option: Bundle? = null) {
        activity.compatStartActivityForResult(createIntent(activity), requestCode, option)
    }

    fun launchForResult(fragment: Fragment, requestCode: Int, option: Bundle? = null) {
        fragment.startActivityForResult(createIntent(fragment.context!!), requestCode, option)
    }

    interface DefaultCompanion {

        fun createLauncher(): ActivityLauncher

        fun launch(context: Context, option: Bundle? = null) {
            createLauncher()
                .launch(context, option)
        }

    }

}

@Suppress("UNCHECKED_CAST")
inline fun <reified T> T.createActivityLauncher(vararg extra: BundlePair,
                                                flags: Int = 0): ActivityLauncher {
    val declaringClass = T::class.java.declaringClass
    return ActivityLauncher(declaringClass as Class<Activity>, *extra, flags = flags)
}


@Suppress("UNCHECKED_CAST")
inline fun <reified T> T.createActivityLauncher(flags: Int = 0): ActivityLauncher {
    val declaringClass = T::class.java.declaringClass
    return ActivityLauncher(declaringClass as Class<Activity>, flags = flags)
}
