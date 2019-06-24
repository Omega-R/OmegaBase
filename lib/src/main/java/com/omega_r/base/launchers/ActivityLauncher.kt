package com.omega_r.base.launchers

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.AndroidRuntimeException
import androidx.core.app.TaskStackBuilder
import androidx.fragment.app.Fragment
import com.omega_r.base.tools.BundlePair
import com.omega_r.base.tools.bundleOf
import com.omega_r.base.tools.equalsBundle
import com.omega_r.base.tools.hashCodeBundle
import kotlinx.android.parcel.Parcelize

/**
 * Created by Anton Knyazev on 06.04.2019.
 */
@Parcelize
class ActivityLauncher(
    private val activityClass: Class<Activity>,
    private val bundle: Bundle? = null,
    private var flags: Int = 0
) : Launcher, Parcelable {

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

    fun addFlags(flag: Int) = apply {
        flags = flags or flag
    }

    fun removeFlags(flag: Int) = apply {
        flags = flags and (flag.inv())
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

    private fun Context.compatStartActivity(intent: Intent, option: Bundle? = null) {
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

    fun getPendingIntent(
        context: Context, requestCode: Int = 0,
        flags: Int = PendingIntent.FLAG_UPDATE_CURRENT
    ): PendingIntent {
        return PendingIntent.getActivity(context, requestCode, createIntent(context), flags)
    }

    fun getPendingIntentWithParentStack(
        context: Context, requestCode: Int = 0,
        flags: Int = PendingIntent.FLAG_UPDATE_CURRENT
    ): PendingIntent {
        return TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(createIntent(context))
            .getPendingIntent(requestCode, flags)!!
    }

    fun launchForResult(activity: Activity, requestCode: Int, option: Bundle? = null) {
        activity.compatStartActivityForResult(createIntent(activity), requestCode, option)
    }

    fun launchForResult(fragment: Fragment, requestCode: Int, option: Bundle? = null) {
        fragment.startActivityForResult(createIntent(fragment.context!!), requestCode, option)
    }

    fun isOurActivity(activity: Activity): Boolean {
        return activityClass.isInstance(activity)
                && activity.intent.extras.equalsBundle(bundle)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ActivityLauncher

        if (activityClass != other.activityClass) return false
        if (!bundle.equalsBundle(other.bundle)) return false
        if (flags != other.flags) return false

        return true
    }

    override fun hashCode(): Int {
        var result = activityClass.hashCode()
        result = 31 * result + (bundle?.hashCodeBundle() ?: 0)
        result = 31 * result + flags
        return result
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
inline fun <reified T> T.createActivityLauncher(
    vararg extra: BundlePair,
    flags: Int = 0
): ActivityLauncher {
    val declaringClass = T::class.java.declaringClass
    return ActivityLauncher(declaringClass as Class<Activity>, *extra, flags = flags)
}


@Suppress("UNCHECKED_CAST")
inline fun <reified T> T.createActivityLauncher(flags: Int = 0): ActivityLauncher {
    val declaringClass = T::class.java.declaringClass
    return ActivityLauncher(declaringClass as Class<Activity>, flags = flags)
}
