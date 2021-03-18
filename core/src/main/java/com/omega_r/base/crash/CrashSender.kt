package com.omega_r.base.crash

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.pm.PackageInfoCompat
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.Exception
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArraySet


class CrashSender(context: Context, private val senderWays: Array<out SenderCrashWay>) : Thread.UncaughtExceptionHandler,
    Application.ActivityLifecycleCallbacks {

    companion object {

        @SuppressLint("StaticFieldLeak")
        private var singleCrashSender: CrashSender? = null

        private val reporters = CopyOnWriteArraySet<CrashReporter>()

        fun setup(application: Application, vararg otherSenderWays: SenderCrashWay) {
            singleCrashSender?.let {
                application.unregisterActivityLifecycleCallbacks(it)
                OmegaUncaughtExceptionHandler.remove(it)
            }
            val handler = CrashSender(application, otherSenderWays)
            OmegaUncaughtExceptionHandler.add(handler)
            application.registerActivityLifecycleCallbacks(handler)
            singleCrashSender = handler
        }

        fun addReporter(reporter: CrashReporter) {
            reporters.add(reporter)
        }

    }

    private val context = context.applicationContext
    private var activityWeakRef: WeakReference<Activity>? = null

    override fun uncaughtException(thread: Thread, error: Throwable) {
        try {
            val currentActivity = activityWeakRef?.get()
            val screenshotBitmap = currentActivity?.createScreenshotBitmap()
            val crashReport = createCrashReport(currentActivity, error, screenshotBitmap)

            senderWays.forEach {
                try {
                    it.send(context, currentActivity, error, crashReport)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            screenshotBitmap?.recycle()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createCrashReport(currentActivity: Activity?, error: Throwable, screenshotBitmap: Bitmap?): CrashReport {
        val info = mutableMapOf<String, Map<String, String>>().apply {
            putGroupMap("OS", getOsInfo())
            putGroupMap("Application", getApplicationInfo())
            putGroupMap("Activity", getActivityInfo(currentActivity))
        }

        reporters.forEach {
            it.report(info)
        }

        return CrashReport(info, getStackTrace(error) ?: "", screenshotBitmap)
    }

    private fun MutableMap<String, Map<String, String>>.putGroupMap(groupName: String, map: Map<String, String>?) {
        if (map?.isNotEmpty() == true) {
            put(groupName, map)
        }
    }

    private fun getActivityInfo(currentActivity: Activity?): Map<String, String>? {
        if (currentActivity != null) {
            val title = currentActivity.title

            return mutableMapOf<String, String>().also { map ->
                currentActivity::class.simpleName?.let { map["ClassName"] = it }
                title?.let { map["title"] = title.toString() }
            }
        }
        return null
    }

    private fun getOsInfo(): Map<String, String>? {
       return mapOf("Version" to "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
    }


    private fun getApplicationInfo(): Map<String, String>? {
        try {
            with(context.packageManager.getPackageInfo(context.packageName, 0)) {
                return mapOf(
                    "Package" to packageName,
                    "Version Name" to versionName,
                    "Version Code" to PackageInfoCompat.getLongVersionCode(this).toString()
                )
            }
        } catch (e: PackageManager.NameNotFoundException) {
            return null // Ignored, this shouldn't happen
        }
    }

    private fun getStackTrace(error: Throwable): String? {
        val stack: Array<StackTraceElement> = error.stackTrace
        if (stack.isNotEmpty()) {
            val stringWriter = StringWriter()
            val printWriter = PrintWriter(stringWriter)
            error.printStackTrace(printWriter)
            return stringWriter.toString()
        }
        return null
    }

    private fun Activity.createScreenshotBitmap(): Bitmap? {
        val window = window ?: return null
        val view = window.decorView
        return getBitmapFromView(view)
    }

    private fun getBitmapFromView(view: View): Bitmap? {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        // nothing
    }

    override fun onActivityStarted(activity: Activity) {
        // nothing
    }

    override fun onActivityResumed(activity: Activity) {
        activityWeakRef = WeakReference(activity)
    }

    override fun onActivityPaused(activity: Activity) {
        activityWeakRef = null
    }

    override fun onActivityStopped(activity: Activity) {
        // nothing
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        // nothing
    }

    override fun onActivityDestroyed(activity: Activity) {
        // nothing
    }

    interface SenderCrashWay {

        fun send(context: Context, currentActivity: Activity?, error: Throwable, crashReport: CrashReport)

    }

    interface CrashReporter {

        fun report(map: MutableMap<String, Map<String, String>>)

    }

}