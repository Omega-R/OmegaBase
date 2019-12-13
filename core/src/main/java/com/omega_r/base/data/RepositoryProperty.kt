package com.omega_r.base.data

import android.os.SystemClock
import com.omega_r.base.data.sources.Source
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Created by Anton Knyazev on 2019-12-13.
 */
class RepositoryProperty<T, C : CoroutineScope, S : Source>(
    private val repository: OmegaRepository<S>,
    private val block: suspend S.(item: T) -> Unit
) : ReadWriteProperty<C, T> {

    private var value: Any? = NULL
    private var lastCacheTime: Long = 0
    private var cacheJob: Job? = null
        set(value) {
            field?.cancel(InnerCancellationException)
            field = value
        }

    private var remoteJob: Job? = null
        set(value) {
            field?.cancel(InnerCancellationException)
            field = value
        }

    constructor(repository: OmegaRepository<S>,block: suspend S.(item: T) -> Unit, value: T): this (repository, block) {
        this.value = value
    }

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: C, property: KProperty<*>) = value as T

    override fun setValue(thisRef: C, property: KProperty<*>, value: T) {
        this.value = value
        cacheJob = saveInCache(thisRef, value)
        remoteJob = saveInRemote(thisRef, value)
    }

    private fun saveInCache(scope: CoroutineScope, item: T): Job {
        val forceSave = SystemClock.elapsedRealtime() - lastCacheTime > 500

        if (forceSave) {
            lastCacheTime = SystemClock.elapsedRealtime()
        }

        return runSaveJob(scope, item, OmegaRepository.Strategy.ONLY_CACHE, 500, forceSave)
    }

    private fun saveInRemote(scope: CoroutineScope, item: T): Job {
        return runSaveJob(scope, item, OmegaRepository.Strategy.ONLY_REMOTE, 1500, false)
    }

    private fun runSaveJob(
        scope: CoroutineScope,
        item: T,
        strategy: OmegaRepository.Strategy,
        timeoutMillis: Long,
        forceSave: Boolean
    ): Job {
        return scope.launch {
            var doSave = true
            try {
                delay(timeoutMillis)
            } catch (e: InnerCancellationException) {
                doSave = forceSave
            } finally {
                if (doSave) {
                    withContext(NonCancellable) {
                        save(scope, strategy, item)
                    }
                }
            }
        }
    }

    private suspend fun save(scope: CoroutineScope, strategy: OmegaRepository.Strategy, item: T) {
        try {
            val channel = repository.createChannel(strategy) {
                block(item)
            }
            channel.receive()
        } catch (e: Throwable) {
            scope.coroutineContext[CoroutineExceptionHandler]?.handleException(coroutineContext, e)
        }
    }

    fun skipSave() {
        cacheJob = null
        remoteJob = null
    }


    private object InnerCancellationException : CancellationException()

    private object NULL


}