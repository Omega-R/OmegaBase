package com.omega_r.base.mvp.presenters

import android.content.pm.PackageManager
import com.omega_r.base.data.OmegaRepository
import com.omega_r.base.data.sources.Source
import com.omega_r.base.mvp.views.OmegaView
import com.omega_r.libs.omegaintentbuilder.OmegaIntentBuilder
import com.omega_r.libs.omegaintentbuilder.interfaces.IntentBuilder
import com.omega_r.libs.omegatypes.Text
import com.omegar.libs.omegalaunchers.ActivityLauncher
import com.omegar.libs.omegalaunchers.BaseIntentLauncher
import com.omegar.libs.omegalaunchers.Launcher
import com.omegar.mvp.MvpPresenter
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import java.io.Serializable
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
private const val REQUEST_PERMISSION_BASE = 10000


open class OmegaPresenter<View : OmegaView> : MvpPresenter<View>(), CoroutineScope {

    private val handler = CoroutineExceptionHandler { _, throwable -> handleErrors(throwable) }

    private val job = SupervisorJob()

    override val coroutineContext: CoroutineContext = Dispatchers.Main + job + handler

    private val permissionsCallbacks: MutableMap<List<String>, ((Boolean) -> Unit)?> by lazy { mutableMapOf<List<String>, ((Boolean) -> Unit)?>() }


    protected val intentBuilder
        get() = OmegaIntentBuilder

    protected open fun handleErrors(throwable: Throwable) {
        throwable.printStackTrace()
    }

    internal open fun attachChildPresenter(childPresenter: OmegaPresenter<*>) {
        childPresenter.attachParentPresenter(this)
    }

    internal open fun attachParentPresenter(parentPresenter: OmegaPresenter<*>) {
        // nothing
    }

    protected suspend fun <T> withWaiting(waitingText: Text? = null, block: suspend () -> T): T {
        withContext(Dispatchers.Main) {
            viewState.setWaiting(true, waitingText)
        }
        return try {
            block()
        } finally {
            viewState.setWaiting(false, waitingText)
        }
    }

    protected fun launchWithWaiting(
        context: CoroutineContext = EmptyCoroutineContext,
        waitingText: Text? = null,
        block: suspend () -> Unit
    ) {
        viewState.setWaiting(true, waitingText)
        launch(context) {
            try {
                block()
            } finally {
                viewState.setWaiting(false, waitingText)
            }
        }
    }

    protected fun <R> ReceiveChannel<R>.request(
        waiting: Boolean = true,
        errorHandler: ((Throwable) -> Boolean)? = null,
        block: (suspend View.(R) -> Unit)? = null
    ) {
        if (waiting) viewState.setWaiting(true)
        val channel = this
        launch {
            var hideWaiting = waiting
            try {
                for (item in channel) {
                    block?.invoke(viewState, item)

                    if (hideWaiting) {
                        hideWaiting = false
                        viewState.setWaiting(false)
                    }
                }
            } catch (e: Throwable) {
                if (errorHandler?.invoke(e) != true) handleErrors(e)
            } finally {
                if (hideWaiting) viewState.setWaiting(false)
            }
        }
    }

    protected fun <S : Source, R> OmegaRepository<S>.request(
        sourceBlock: suspend S.() -> R,
        strategy: OmegaRepository.Strategy = OmegaRepository.Strategy.CACHE_AND_REMOTE,
        waiting: Boolean = true,
        errorHandler: ((Throwable) -> Boolean)? = null,
        viewStateBlock: suspend View.(R) -> Unit
    ) {
        createChannel(strategy, sourceBlock).request(
            waiting = waiting,
            errorHandler = errorHandler,
            block = viewStateBlock
        )
    }

    protected fun <S : Source> OmegaRepository<S>.request(
        strategy: OmegaRepository.Strategy = OmegaRepository.Strategy.CACHE_AND_REMOTE,
        waiting: Boolean = true,
        sourceBlock: suspend S.() -> Unit
    ) {
        createChannel(strategy, sourceBlock)
            .request(waiting = waiting)
    }

    fun hideQueryOrMessage() = viewState.hideQueryOrMessage()

    protected open fun Launcher.launch() {
        try {
            viewState.launch(this)
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

    protected fun BaseIntentLauncher.launchForResult(requestCode: Int) {
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

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    @Suppress("UNUSED_PARAMETER")
    protected fun getPermissionState(permissionName: String): Boolean {
        return false
    }

    fun requestPermission(vararg permissions: String, resultCallback: (Boolean) -> Unit) {
        val permissionList = permissions.toList()
        permissionsCallbacks[permissionList] = resultCallback

        viewState.requestPermissions(
            REQUEST_PERMISSION_BASE + permissionsCallbacks.keys.indexOf(
                permissionList
            ), *permissions
        )
    }

    fun onPermissionResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ): Boolean {
        val permissionList = permissions.toList()
        if (requestCode >= REQUEST_PERMISSION_BASE && permissionsCallbacks.contains(permissionList)) {
            permissionsCallbacks[permissionList]?.invoke(grantResults.firstOrNull { it != PackageManager.PERMISSION_GRANTED } == null)
            permissionsCallbacks[permissionList] = null
            return true
        } else {
            val permissionResults =
                permissions.mapIndexed { index, permission -> permission to (grantResults[index] == PackageManager.PERMISSION_GRANTED) }
                    .toMap()

            return onPermissionResult(requestCode, permissionResults)
        }
    }

    protected open fun onPermissionResult(
        requestCode: Int,
        permissionResults: Map<String, Boolean>
    ): Boolean {
        return false
    }


}