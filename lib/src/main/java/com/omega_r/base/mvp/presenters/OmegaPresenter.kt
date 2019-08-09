package com.omega_r.base.mvp.presenters

import com.omega_r.base.data.OmegaRepository
import com.omega_r.base.data.sources.Source
import com.omega_r.base.mvp.views.OmegaView
import com.omega_r.libs.omegaintentbuilder.OmegaIntentBuilder
import com.omega_r.libs.omegaintentbuilder.interfaces.IntentBuilder
import com.omegar.libs.omegalaunchers.ActivityLauncher
import com.omegar.libs.omegalaunchers.BaseIntentLauncher
import com.omegar.libs.omegalaunchers.Launcher
import com.omegar.mvp.MvpPresenter
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import java.io.Serializable
import kotlin.coroutines.CoroutineContext

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
open class OmegaPresenter<View : OmegaView> : MvpPresenter<View>(), CoroutineScope {

    private val handler = CoroutineExceptionHandler { _, throwable -> handleErrors(throwable) }

    private val job = SupervisorJob()

    override val coroutineContext: CoroutineContext = Dispatchers.Main + job + handler

    protected val intentBuilder
        get() = OmegaIntentBuilder

    protected open fun handleErrors(throwable: Throwable) {
        throwable.printStackTrace()
    }

    protected suspend fun <T> withWaiting(block: suspend () -> T): T = with(viewState) {
        setWaiting(true)
        try {
            block()
        } finally {
            setWaiting(false)
        }
    }

    protected inline fun launchWithWaiting(crossinline block: suspend () -> Unit) = with(viewState) {
        launch {
            try {
                block()
            } finally {
                setWaiting(false)
            }
        }
    }


    protected fun <R> ReceiveChannel<R>.request(waiting: Boolean = true, block: (suspend View.(R) -> Unit)? = null) {
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
            } finally {
                if (hideWaiting) {
                    viewState.setWaiting(false)
                }
            }
        }
    }

    protected fun <S : Source, R> OmegaRepository<S>.request(
        sourceBlock: suspend S.() -> R,
        strategy: OmegaRepository.Strategy = OmegaRepository.Strategy.CACHE_AND_REMOTE,
        waiting: Boolean = true,
        viewStateBlock: suspend View.(R) -> Unit
    ) {
        createChannel(strategy, sourceBlock).request(waiting = waiting, block = viewStateBlock)
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
        viewState.launch(this)
    }

    protected fun ActivityLauncher.DefaultCompanion.launch() {
        viewState.launch(createLauncher())
    }

    protected fun BaseIntentLauncher.launchForResult(requestCode: Int) {
        viewState.launchForResult(this, requestCode)
    }

    protected fun IntentBuilder.launch() = createLauncher().launch()

    protected fun IntentBuilder.launchForResult(requestCode: Int) = createLauncher().launchForResult(requestCode)

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


}