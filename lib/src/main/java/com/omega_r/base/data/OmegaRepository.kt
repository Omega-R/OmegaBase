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
open class OmegaRepository<SOURCE : Source>(vararg sources: SOURCE) {

    private val job = SupervisorJob()

    protected val coroutineScope = CoroutineScope(Dispatchers.Default + job)

    protected val remoteSource = sources.firstOrNull { it.type == Source.Type.REMOTE }

    protected val memoryCacheSource =
        sources.firstOrNull { it.type == Source.Type.MEMORY_CACHE } as? CacheSource

    protected val fileCacheSource =
        sources.firstOrNull { it.type == Source.Type.FILE_CACHE } as? CacheSource

    protected val defaultSource = sources.firstOrNull { it.type == Source.Type.DEFAULT }

    protected open suspend fun <R> processResult(result: R, sourceType: Source.Type): R {
        return result
    }

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
                val result = processResult(block(remoteSource), Source.Type.REMOTE)
                send(result)
                memoryCacheSource?.update(result)
                fileCacheSource?.update(result)
                return
            }
        }

        if (defaultSource != null) {
            ignoreException {
                return send(processResult(block(defaultSource), Source.Type.DEFAULT))
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
                send(processResult(block(memoryCacheSource as SOURCE), Source.Type.MEMORY_CACHE))
                return
            }
        }

        if (fileCacheSource != null) {
            ignoreException {
                val result = processResult(block(fileCacheSource as SOURCE), Source.Type.FILE_CACHE)
                send(result)
                memoryCacheSource?.update(result)
                return
            }
        }

        if (defaultSource != null) {
            ignoreException {
                return send(processResult(block(defaultSource), Source.Type.DEFAULT))
            }
        }

        throwNoData("Cache sources is null")
    }

    private suspend fun <R> ProducerScope<R>.applyRemoteElseCache(block: suspend SOURCE.() -> R) {
        var remoteException: Exception? = null
        if (remoteSource != null) {
            remoteException = ignoreException {
                val result = processResult(block(remoteSource), Source.Type.REMOTE)
                send(result)
                memoryCacheSource?.update(result)
                fileCacheSource?.update(result)
                return
            }
        }

        var cacheException: Exception? = null

        if (memoryCacheSource != null) {
            cacheException = ignoreException {
                send(processResult(block(memoryCacheSource as SOURCE), Source.Type.MEMORY_CACHE))
                return
            }
        }

        if (fileCacheSource != null) {
            cacheException = ignoreException {
                val result = processResult(block(fileCacheSource as SOURCE), Source.Type.FILE_CACHE)
                send(result)
                memoryCacheSource?.update(result)
                return
            }
        }

        if (defaultSource != null) {
            cacheException = ignoreException {
                return send(processResult(block(defaultSource), Source.Type.DEFAULT))
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
                send(processResult(block(memoryCacheSource as SOURCE), Source.Type.MEMORY_CACHE))
                return
            }
        }
        if (fileCacheSource != null) {
            cacheException = ignoreException {
                val result = processResult(block(fileCacheSource as SOURCE), Source.Type.FILE_CACHE)
                send(result)
                memoryCacheSource?.update(result)
                return
            }
        }

        var remoteException: Exception? = null

        if (remoteSource != null) {
            remoteException = ignoreException {
                val result = processResult(block(remoteSource), Source.Type.REMOTE)
                send(result)
                memoryCacheSource?.update(result)
                fileCacheSource?.update(result)
                return
            }
        }

        if (defaultSource != null) {
            cacheException = ignoreException {
                return send(processResult(block(defaultSource), Source.Type.DEFAULT))
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
                    val result = processResult(block(memoryCacheSource as SOURCE), Source.Type.MEMORY_CACHE)

                    if (isActive && !isClosedForSend) {
                        send(result)
                    }
                    return@async null
                }
            }
            if (fileCacheSource != null) {
                cacheException = ignoreException {
                    val result = processResult(block(fileCacheSource as SOURCE), Source.Type.FILE_CACHE)
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
                        send(processResult(block(defaultSource), Source.Type.DEFAULT))
                    }
                    return@async null
                }
            }

            return@async cacheException
        }

        val remoteException = if (remoteSource != null) {
            ignoreException {
                val result = processResult(block(remoteSource), Source.Type.REMOTE)
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