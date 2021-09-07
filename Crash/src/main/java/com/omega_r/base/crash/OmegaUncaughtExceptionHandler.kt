package com.omega_r.base.crash

import java.lang.Thread.UncaughtExceptionHandler
import java.util.concurrent.CopyOnWriteArraySet

object OmegaUncaughtExceptionHandler : UncaughtExceptionHandler {

    private var defaultHandler: UncaughtExceptionHandler? = null

    private val uncaughtExceptionHandlers = CopyOnWriteArraySet<UncaughtExceptionHandler>()

   init {
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread, error: Throwable) {
        try {
            uncaughtExceptionHandlers.forEach {
                it.uncaughtException(thread, error)
            }
        } finally {
            defaultHandler?.uncaughtException(thread, error)
        }
    }

    fun add(handler: UncaughtExceptionHandler) {
        uncaughtExceptionHandlers.add(handler)
    }

    fun remove(handler: UncaughtExceptionHandler) {
        uncaughtExceptionHandlers.remove(handler)
    }

}