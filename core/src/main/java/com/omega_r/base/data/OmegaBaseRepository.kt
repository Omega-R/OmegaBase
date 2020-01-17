package com.omega_r.base.data

import com.omega_r.base.data.Strategy.*
import com.omega_r.base.data.sources.CacheSource
import com.omega_r.base.data.sources.Source
import com.omega_r.base.errors.AppException
import com.omega_r.base.errors.ErrorHandler
import com.omega_r.base.errors.throwNoData
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce

/**
 * Created by Anton Knyazev on 2019-05-28.
 */
@Suppress("UNCHECKED_CAST", "MemberVisibilityCanBePrivate")
@UseExperimental(ExperimentalCoroutinesApi::class)
open class OmegaRepository<SOURCE : Source>(protected val errorHandler: ErrorHandler, vararg sources: SOURCE) {

    private val job = SupervisorJob()

    protected val coroutineScope = CoroutineScope(Dispatchers.Default + job)

    protected val remoteSource: SOURCE? = sources.firstOrNull { it.type == Source.Type.REMOTE }

    protected val memorySource: SOURCE?
        get() = memoryCacheSource as SOURCE?

    protected val fileSource: SOURCE?
        get() = fileCacheSource as SOURCE?

    protected val defaultSource: SOURCE? = sources.firstOrNull { it.type == Source.Type.DEFAULT }

    private val memoryCacheSource =
        sources.firstOrNull { it.type == Source.Type.MEMORY_CACHE } as? CacheSource

    private val fileCacheSource =
        sources.firstOrNull { it.type == Source.Type.FILE_CACHE } as? CacheSource

    protected open suspend fun <R> processResult(result: R, sourceType: Source.Type): R {
        return result
    }

    protected fun <R> createChannel(strategy: Strategy, block: suspend SOURCE.() -> R): ReceiveChannel<R> {
        return coroutineScope.produce {
            try {
                when (strategy) {
                    CACHE_AND_REMOTE -> applyCacheAndRemote(block)
                    ONLY_REMOTE -> applyOnlyRemote(block)
                    ONLY_CACHE -> applyOnlyCache(block)
                    REMOTE_ELSE_CACHE -> applyRemoteElseCache(block)
                    CACHE_ELSE_REMOTE -> applyCacheElseRemote(block)
                    MEMORY_ELSE_CACHE_AND_REMOTE -> applyMemoryElseCacheAndRemote(block)
                }
            } catch (e: Throwable) {
                throw errorHandler.handleThrowable(e)
            }
        }
    }

    private suspend fun <R> ProducerScope<R>.applyOnlyRemote(block: suspend SOURCE.() -> R) {
        var remoteException: Exception? = null

        if (remoteSource != null) {
            remoteException = getException {
                val result = processResult(block(remoteSource), Source.Type.REMOTE)
                send(result)
                memoryCacheSource?.update(result)
                fileCacheSource?.update(result)
                return
            }
        }


        if (defaultSource != null) {
            val cacheException: Exception? = getException {
                return send(processResult(block(defaultSource), Source.Type.DEFAULT))
            }
            cacheException?.printStackTraceIfNeeded()
        }

        if (remoteException != null) {
            throw remoteException
        } else {
            throwNoData("Remote sources is null")
        }
    }

    private suspend fun <R> ProducerScope<R>.applyOnlyCache(block: suspend SOURCE.() -> R) {
        var cacheException: Exception? = null

        if (memoryCacheSource != null) {
            cacheException = getException {
                send(processResult(block(memoryCacheSource as SOURCE), Source.Type.MEMORY_CACHE))
                return
            }
        }

        if (fileCacheSource != null) {
            cacheException?.printStackTraceIfNeeded()
            cacheException = getException {
                val result = processResult(block(fileCacheSource as SOURCE), Source.Type.FILE_CACHE)
                send(result)
                memoryCacheSource?.update(result)
                return
            }
        }

        if (defaultSource != null) {
            cacheException?.printStackTraceIfNeeded()
            cacheException = getException {
                return send(processResult(block(defaultSource), Source.Type.DEFAULT))
            }
            cacheException?.printStackTraceIfNeeded()
        }

        throwNoData("Cache sources is null")
    }

    private suspend fun <R> ProducerScope<R>.applyRemoteElseCache(block: suspend SOURCE.() -> R) {
        var remoteException: Exception? = null
        if (remoteSource != null) {
            remoteException = getException {
                val result = processResult(block(remoteSource), Source.Type.REMOTE)
                send(result)
                memoryCacheSource?.update(result)
                fileCacheSource?.update(result)
                return
            }
        }

        var cacheException: Exception? = null

        if (memoryCacheSource != null) {
            cacheException?.printStackTraceIfNeeded()
            cacheException = getException {
                send(processResult(block(memoryCacheSource as SOURCE), Source.Type.MEMORY_CACHE))
                return
            }
        }

        if (fileCacheSource != null) {
            cacheException?.printStackTraceIfNeeded()
            cacheException = getException {
                val result = processResult(block(fileCacheSource as SOURCE), Source.Type.FILE_CACHE)
                send(result)
                memoryCacheSource?.update(result)
                return
            }
        }

        if (defaultSource != null) {
            cacheException?.printStackTraceIfNeeded()
            cacheException = getException {
                return send(processResult(block(defaultSource), Source.Type.DEFAULT))
            }
        }
        if (remoteSource == null) {
            cacheException?.let { throw it }
        } else {
            cacheException?.printStackTraceIfNeeded()
            remoteException?.let { throw it }
        }
    }

    private suspend fun <R> ProducerScope<R>.applyCacheElseRemote(block: suspend SOURCE.() -> R) {

        var cacheException: Exception? = null

        if (memoryCacheSource != null) {
            cacheException = getException {
                send(processResult(block(memoryCacheSource as SOURCE), Source.Type.MEMORY_CACHE))
                return
            }
        }
        if (fileCacheSource != null) {
            cacheException?.printStackTraceIfNeeded()
            cacheException = getException {
                val result = processResult(block(fileCacheSource as SOURCE), Source.Type.FILE_CACHE)
                send(result)
                memoryCacheSource?.update(result)
                return
            }
        }

        var remoteException: Exception? = null

        if (remoteSource != null) {
            remoteException = getException {
                val result = processResult(block(remoteSource), Source.Type.REMOTE)
                send(result)
                memoryCacheSource?.update(result)
                fileCacheSource?.update(result)
                return
            }
        }

        if (defaultSource != null) {
            cacheException?.printStackTraceIfNeeded()
            cacheException = getException {
                return send(processResult(block(defaultSource), Source.Type.DEFAULT))
            }
        }

        when {
            remoteException != null -> {
                cacheException?.printStackTraceIfNeeded()
                throw remoteException
            }
            cacheException != null -> {
                throw cacheException
            }
            else -> {
                throwNoData("Cache sources is null")
            }
        }
    }

    private suspend fun <R> ProducerScope<R>.applyCacheAndRemote(block: suspend SOURCE.() -> R) {
        val cacheReturnDeferred = coroutineScope {
            async {
                var cacheException: Exception? = null
                if (memoryCacheSource != null) {
                    cacheException = getException {
                        val result =
                            processResult(block(memoryCacheSource as SOURCE), Source.Type.MEMORY_CACHE)

                        if (isActive && !isClosedForSend) {
                            send(result)
                        }
                        return@async null
                    }
                }
                if (fileCacheSource != null) {
                    cacheException?.printStackTraceIfNeeded()
                    cacheException = getException {
                        val result =
                            processResult(block(fileCacheSource as SOURCE), Source.Type.FILE_CACHE)
                        if (isActive && !isClosedForSend) {
                            send(result)
                            memoryCacheSource?.update(result)
                        }
                        return@async null
                    }
                }


                if (defaultSource != null) {
                    cacheException?.printStackTraceIfNeeded()
                    cacheException = getException {
                        if (isActive && !isClosedForSend) {
                            send(processResult(block(defaultSource), Source.Type.DEFAULT))
                        }
                        return@async null
                    }
                }

                return@async cacheException
            }
        }

        val remoteException = remoteSource?.let {
            getException {
                val result = processResult(block(remoteSource), Source.Type.REMOTE)
                cacheReturnDeferred.cancel()
                send(result)
                channel.close()
                memoryCacheSource?.update(result)
                fileCacheSource?.update(result)
                return
            }
        }

        val cacheException = cacheReturnDeferred.await()

        if (remoteSource == null) {
            if (cacheException != null) {
                throw cacheException
            }
        } else {
            cacheException?.printStackTraceIfNeeded()
            if (remoteException != null && (cacheException != null || remoteException !is AppException.NoData))
                throw remoteException
        }

    }

    private suspend fun <R> ProducerScope<R>.applyMemoryElseCacheAndRemote(block: suspend SOURCE.() -> R) {
        if (memoryCacheSource != null) {
            getException {
                send(block(memoryCacheSource as SOURCE))
                return
            }
        }
        applyCacheAndRemote(block)
    }

    fun clearCache() {
        memoryCacheSource?.clear()
        fileCacheSource?.clear()
    }

    private inline fun getException(block: () -> Unit): Exception? {
        return try {
            block()
            null
        } catch (exception: Exception) {
            exception
        }
    }

    private fun Exception.printStackTraceIfNeeded() {
        if (this is AppException.NoData) {
            printStackTrace()
        }
    }

}