package com.omega_r.base.components

import com.omegar.mvp.MvpPresenter
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
open class OmegaPresenter<View: OmegaView>: MvpPresenter<View>(), CoroutineScope {

    private val handler = CoroutineExceptionHandler { _, exception ->
        viewState.setWaiting(false)
        handleErrors(exception)
    }

    private val job = SupervisorJob()

    override val coroutineContext: CoroutineContext = Dispatchers.Main + job + handler

    protected open fun handleErrors(throwable: Throwable) {
        throwable.printStackTrace()
    }

    protected suspend fun <T> CoroutineScope.withWaiting(block: suspend () -> T): T {
        viewState.setWaiting(true)
        val result = block()
        viewState.setWaiting(false)
        return result
    }

    protected inline fun launchWithWaiting(crossinline block: suspend () -> Unit) {
        viewState.setWaiting(true)
        launch {
            block()
            viewState.setWaiting(false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

}