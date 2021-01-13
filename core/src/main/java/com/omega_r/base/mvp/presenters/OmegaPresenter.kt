package com.omega_r.base.mvp.presenters

import android.content.pm.PackageManager.PERMISSION_GRANTED
import com.omega_r.base.R
import com.omega_r.base.errors.AppException
import com.omega_r.base.logs.log
import com.omega_r.base.mvp.views.OmegaView
import com.omega_r.libs.omegaintentbuilder.OmegaIntentBuilder
import com.omega_r.libs.omegaintentbuilder.interfaces.IntentBuilder
import com.omega_r.libs.omegatypes.Text
import com.omega_r.libs.omegatypes.toText
import com.omegar.libs.omegalaunchers.ActivityLauncher
import com.omegar.libs.omegalaunchers.BaseIntentLauncher
import com.omegar.libs.omegalaunchers.DialogFragmentLauncher
import com.omegar.libs.omegalaunchers.Launcher
import com.omegar.mvp.MvpPresenter
import kotlinx.coroutines.*
import java.io.PrintWriter
import java.io.Serializable
import java.io.StringWriter
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
private const val REQUEST_PERMISSION_BASE = 10000


open class OmegaPresenter<View : OmegaView> : MvpPresenter<View>(), CoroutineScope {

    companion object {

        internal var isDebuggable : Boolean? = null

    }

    private val handler = CoroutineExceptionHandler { _, throwable ->
        this@OmegaPresenter.launch {
            onError(throwable)
        }
    }

    private val job = SupervisorJob()

    override val coroutineContext: CoroutineContext = Dispatchers.Main + job + handler

    private val permissionsCallbacks: MutableMap<List<String>, ((Boolean) -> Unit)?>
            by lazy { mutableMapOf<List<String>, ((Boolean) -> Unit)?>() }

    protected val intentBuilder
        get() = OmegaIntentBuilder

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun onError(throwable: Throwable) {
        log(throwable)
        handleErrors(throwable)
    }

    protected open fun handleErrors(throwable: Throwable) {
        viewState.showMessage(getErrorMessage(throwable))
    }

    protected open fun getErrorMessage(throwable: Throwable): Text {
        return when (throwable) {
            is OutOfMemoryError -> Text.from(R.string.error_out_of_memory)
            is AppException.NoConnection -> Text.from(R.string.error_no_connection)
            is AppException.ServerUnavailable -> Text.from(R.string.error_server_unavailable)
            is AppException.NotFound,
            is AppException.ServerProblem -> Text.from(R.string.error_server_problem)
            is AppException.AccessDenied -> Text.from(R.string.error_access_denied)
            is AppException.NotAuthorized -> Text.from(R.string.error_not_authorized)
            is AppException.AuthorizedFailed -> Text.from(R.string.error_authorization_failed)
            else -> getUnknownErrorMessage(throwable)
        }
    }

    protected open fun getUnknownErrorMessage(throwable: Throwable): Text {
        return if (isDebuggable == true) {
            var causeThrowable = throwable

            while (causeThrowable.cause != null) {
                causeThrowable = causeThrowable.cause!!
            }
            StringWriter()
                .apply { causeThrowable.printStackTrace(PrintWriter(this)) }
                .toString()
                .toText()
        } else {
            Text.from(R.string.error_unknown)
        }
    }

    internal fun attachChildPresenter(childPresenter: OmegaPresenter<*>) {
        onAttachChildPresenter(childPresenter)
        childPresenter.onAttachParentPresenter(this)
    }

    protected open fun onAttachChildPresenter(childPresenter: OmegaPresenter<*>) {
        // nothing
    }

    protected open fun onAttachParentPresenter(parentPresenter: OmegaPresenter<*>) {
        // nothing
    }

    internal fun detachChildPresenter(childPresenter: OmegaPresenter<*>) {
        onDetachChildPresenter(childPresenter)
        childPresenter.onDetachParentPresenter(this)
    }

    protected open fun onDetachChildPresenter(parentPresenter: OmegaPresenter<*>) {
        // nothing
    }

    protected open fun onDetachParentPresenter(parentPresenter: OmegaPresenter<*>) {
        // nothing
    }

    protected suspend fun <T> withWaiting(waitingText: Text? = null, block: suspend () -> T): T {
        withContext(Dispatchers.Main) {
            viewState.setWaiting(true, waitingText)
        }
        return try {
            block()
        } finally {
            withContext(Dispatchers.Main) {
                viewState.setWaiting(false, waitingText)
            }
        }
    }

    protected fun launchWithWaiting(
        context: CoroutineContext = EmptyCoroutineContext,
        waitingText: Text? = null,
        block: suspend () -> Unit
    ): Job {
        viewState.setWaiting(true, waitingText)
        return launch(context) {
            try {
                block()
            } finally {
                withContext(Dispatchers.Main) {
                    viewState.setWaiting(false, waitingText)
                }
            }
        }
    }

    fun hideQueryOrMessage() = viewState.hideQueryOrMessage()

    protected open fun Launcher.launch() {
        try {
            viewState.launch(this)
        } catch (e: Throwable) {
            handleErrors(e)
        }
    }

    protected fun ActivityLauncher.launch(vararg launchers: ActivityLauncher) {
        try {
            viewState.launch(this, *launchers)
        } catch (e: Throwable) {
            handleErrors(e)
        }
    }

    protected fun ActivityLauncher.DefaultCompanion.launch() {
        try {
            viewState.launch(createLauncher())
        } catch (e: Throwable) {
            handleErrors(e)
        }
    }

    protected fun ActivityLauncher.DefaultCompanion.launch(vararg launchers: ActivityLauncher) {
        try {
            viewState.launch(createLauncher(), *launchers)
        } catch (e: Throwable) {
            handleErrors(e)
        }
    }

    protected fun BaseIntentLauncher.launchForResult(requestCode: Int) {
        try {
            viewState.launchForResult(this, requestCode)
        } catch (e: Throwable) {
            handleErrors(e)
        }
    }

    protected fun DialogFragmentLauncher.launchForResult(requestCode: Int) {
        try {
            viewState.launchForResult(this, requestCode)
        } catch (e: Throwable) {
            handleErrors(e)
        }
    }

    protected fun IntentBuilder.launch() = createLauncher().launch()

    protected fun IntentBuilder.launchForResult(requestCode: Int) =
        createLauncher().launchForResult(requestCode)

    open fun onLaunchResult(requestCode: Int, success: Boolean, data: Serializable?): Boolean {
        return false
    }

    protected fun setResult(success: Boolean, data: Serializable? = null) {
        viewState.setResult(success, data)
    }

    protected fun exit(success: Boolean = false, data: Serializable? = null) {
        setResult(success, data)
        viewState.exit()
    }

    @Suppress("UNUSED_PARAMETER")
    protected suspend fun getPermissionState(permissionName: String): Boolean {
        val deferred = CompletableDeferred<Boolean>()
        viewState.requestGetPermission(permissionName, deferred)
        return deferred.await()
    }

    protected fun requestPermission(vararg permissions: String, resultCallback: (Boolean) -> Unit) {
        val permissionList = permissions.toList()
        permissionsCallbacks[permissionList] = resultCallback

        val requestCode =
            REQUEST_PERMISSION_BASE + permissionsCallbacks.keys.indexOf(permissionList)

        viewState.requestPermissions(requestCode, *permissions)
    }

    fun onPermissionResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ): Boolean {
        val permissionList = permissions.toList()
        if (requestCode >= REQUEST_PERMISSION_BASE && permissionsCallbacks.contains(permissionList)){
            val success =
                grantResults.firstOrNull { it != PERMISSION_GRANTED } == null
            permissionsCallbacks[permissionList]?.invoke(success)
            permissionsCallbacks[permissionList] = null
            return true
        } else {
            val permissionResults =
                permissions.mapIndexed { index, permission ->
                    val success = grantResults[index] == PERMISSION_GRANTED
                    permission to success
                }.toMap()

            return onPermissionResult(requestCode, permissionResults)
        }
    }

    protected open fun onPermissionResult(
        requestCode: Int,
        permissionResults: Map<String, Boolean>
    ): Boolean {
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

}