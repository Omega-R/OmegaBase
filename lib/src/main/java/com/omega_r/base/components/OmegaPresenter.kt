package com.omega_r.base.components

import com.omegar.mvp.MvpPresenter
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
        // nothing
    }

    protected suspend fun <T> CoroutineScope.withWaiting(block: suspend () -> T): T {
        viewState.setWaiting(true)
        val result = block()
        viewState.setWaiting(false)
        return result
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

}