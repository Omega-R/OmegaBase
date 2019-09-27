package com.omega_r.base.data

import com.omega_r.base.data.OmegaRepository.Strategy.*
import com.omega_r.base.data.sources.CacheSource
import com.omega_r.base.data.sources.Source
import com.omega_r.base.errors.AppException
import com.omega_r.base.errors.throwNoData
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce

/**
 * Created by Anton Knyazev on 2019-05-28.
 */
@Suppress("UNCHECKED_CAST")
@UseExperimental(ExperimentalCoroutinesApi::class)
class OmegaRepository<SOURCE : Source>(vararg sources: SOURCE) {

    private val job = SupervisorJob()

    private val coroutineScope = CoroutineScope(Dispatchers.Default + job)

    private val remoteSource = sources.firstOrNull { it.type == Source.Type.REMOTE }

    private val memoryCacheSource =
        sources.firstOrNull { it.type == Source.Type.MEMORY_CACHE } as? CacheSource

    private val fileCacheSource =
        sources.firstOrNull { it.type == Source.Type.FILE_CACHE } as? CacheSource

    private val defaultSource = sources.firstOrNull { it.type == Source.Type.DEFAULT }

    fun <R> createChannel(
        strategy: Strategy = CACHE_AND_REMOTE,
        block: suspend SOURCE.() -> R
    ): ReceiveChannel<R> {
        return coroutineScope.produce {
            when (strategy) {
                ONLY_REMOTE -> applyOnlyRemote(block)
                ONLY_CACHE -> applyOnlyCache(block)
                REMOTE_ELSE_CACHE -> applyRemoteElseCache(block)
                CACHE_ELSE_REMOTE -> applyCacheElseRemote(block)
                CACHE_AND_REMOTE -> applyCacheAndRemote(block)
                MEMORY_ELSE_CACHE_AND_REMOTE -> {
                    if (memoryCacheSource != null) {
                        ignoreException {
                            send(block(memoryCacheSource as SOURCE))
                            return@produce
                        }
                    }
                    applyCacheAndRemote(block)
                }
            }
        }
    }


    private suspend fun <R> ProducerScope<R>.applyOnlyRemote(block: suspend SOURCE.() -> R) {
        var remoteException: Exception? = null

        if (remoteSource != null) {
            remoteException = ignoreException {
                val result = block(remoteSource)
                send(result)
                memoryCacheSource?.update(result)
                fileCacheSource?.update(result)
                return
            }
        }

        if (defaultSource != null) {
            ignoreException {
                return send(block(defaultSource))
            }
        }

        if (remoteException != null) {
            throw remoteException
        } else {
            throwNoData("Remote sources is null")
        }
    }

    private suspend fun <R> ProducerScope<R>.applyOnlyCache(block: suspend SOURCE.() -> R) {
        if (memoryCacheSource != null) {
            ignoreException {
                send(block(memoryCacheSource as SOURCE))
                return
            }
        }

        if (fileCacheSource != null) {
            ignoreException {
                val result = block(fileCacheSource as SOURCE)
                send(result)
                memoryCacheSource?.update(result)
                return
            }
        }

        if (defaultSource != null) {
            ignoreException {
                return send(block(defaultSource))
            }
        }

        throwNoData("Cache sources is null")
    }

    private suspend fun <R> ProducerScope<R>.applyRemoteElseCache(block: suspend SOURCE.() -> R) {
        var remoteException: Exception? = null
        if (remoteSource != null) {
            remoteException = ignoreException {
                val result = block(remoteSource)
                send(result)
                memoryCacheSource?.update(result)
                fileCacheSource?.update(result)
                return
            }
        }

        var cacheException: Exception? = null

        if (memoryCacheSource != null) {
            cacheException = ignoreException {
                send(block(memoryCacheSource as SOURCE))
                return
            }
        }

        if (fileCacheSource != null) {
            cacheException = ignoreException {
                val result = block(fileCacheSource as SOURCE)
                send(result)
                memoryCacheSource?.update(result)
                return
            }
        }

        if (defaultSource != null) {
            cacheException = ignoreException {
                return send(block(defaultSource))
            }
        }
        if (remoteSource == null) {
            cacheException?.let { throw it }
        } else {
            remoteException?.let { throw it }
        }
    }

    private suspend fun <R> ProducerScope<R>.applyCacheElseRemote(block: suspend SOURCE.() -> R) {

        var cacheException: Exception? = null

        if (memoryCacheSource != null) {
            cacheException = ignoreException {
                send(block(memoryCacheSource as SOURCE))
                return
            }
        }
        if (fileCacheSource != null) {
            cacheException = ignoreException {
                val result = block(fileCacheSource as SOURCE)
                send(result)
                memoryCacheSource?.update(result)
                return
            }
        }

        var remoteException: Exception? = null

        if (remoteSource != null) {
            remoteException = ignoreException {
                val result = block(remoteSource)
                send(result)
                memoryCacheSource?.update(result)
                fileCacheSource?.update(result)
                return
            }
        }

        if (defaultSource != null) {
            cacheException = ignoreException {
                return send(block(defaultSource))
            }
        }

        if (remoteException != null) {
            throw remoteException
        } else if (cacheException != null) {
            throw cacheException
        } else {
            throwNoData("Cache sources is null")
        }
    }

    private suspend fun <R> ProducerScope<R>.applyCacheAndRemote(block: suspend SOURCE.() -> R) {
        val cacheReturnDeferred = async {
            var cacheException: Exception? = null
            if (memoryCacheSource != null) {
                cacheException = ignoreException {
                    val result = block(memoryCacheSource as SOURCE)

                    if (isActive && !isClosedForSend) {
                        send(result)
                    }
                    return@async null
                }
            }
            if (fileCacheSource != null) {
                cacheException = ignoreException {
                    val result = block(fileCacheSource as SOURCE)
                    if (isActive && !isClosedForSend) {
                        send(result)
                        memoryCacheSource?.update(result)
                    }
                    return@async null
                }
            }

            if (defaultSource != null) {
                cacheException = ignoreException {
                    if (isActive && !isClosedForSend) {
                        send(block(defaultSource))
                    }
                    return@async null
                }
            }

            return@async cacheException
        }

        val remoteException = if (remoteSource != null) {
            ignoreException {
                val result = block(remoteSource)
                cacheReturnDeferred.cancel()
                send(result)
                channel.close()
                memoryCacheSource?.update(result)
                fileCacheSource?.update(result)
                return
            }
        } else {
            null
        }

        val cacheException = cacheReturnDeferred.await()

        if (remoteSource == null) {
            if (cacheException != null) {
                throw cacheException
            }
        } else {
            if (remoteException != null && (cacheException != null || remoteException !is AppException.NoData))
                throw remoteException
        }

    }

    fun clearCache() {
        memoryCacheSource?.clear()
        fileCacheSource?.clear()
    }

    enum class Strategy {
        ONLY_REMOTE,
        REMOTE_ELSE_CACHE,
        CACHE_ELSE_REMOTE,
        CACHE_AND_REMOTE,
        MEMORY_ELSE_CACHE_AND_REMOTE,
        ONLY_CACHE

    }

    private inline fun ignoreException(block: () -> Unit): Exception? {
        return try {
            block()
            null
        } catch (exception: Exception) {
            exception
        }
    }

}