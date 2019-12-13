package com.omega_r.base.simple

import com.omega_r.base.data.OmegaRepository
import com.omega_r.base.data.OmegaRepository.Strategy.*
import com.omega_r.base.data.sources.CacheSource
import com.omega_r.base.data.sources.Source
import com.omega_r.base.errors.AppException
import com.omega_r.base.errors.ErrorHandler
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.receiveOrNull
import kotlinx.coroutines.flow.*

class TestRepository(errorHandler: ErrorHandler) :
    OmegaRepository<MainSource>(errorHandler, RemoteSource(), CacheSource()) {

    fun testMethodReturn(strategy: OmegaRepository.Strategy, kek: String): ReceiveChannel<String> {
        return createChannel(strategy) { testMethodReturn(kek) }
    }

    fun <T> createSaveSession(
        scope: CoroutineScope,
        errorHandler: (remote: Boolean, Throwable) -> Unit,
        block: suspend MainSource.(item: T) -> Unit
    ): SaveSingleSession<T> {
        return SaveSingleSession(scope, errorHandler = errorHandler, block = block)
    }

    inner class SaveSingleSession<T>(
        scope: CoroutineScope,
        cachePeriodMillis: Long = 500L,
        remotePeriodMillis: Long = 1000L,
        private val errorHandler: (remote: Boolean, Throwable) -> Unit,
        private val block: suspend MainSource.(item: T) -> Unit
    ) {

        private val saveChannel = Channel<T>(capacity = Channel.UNLIMITED)

        private var lastItem: T? = null

        private var lastCacheSave: Boolean = false

        init {
            open(scope, cachePeriodMillis, remotePeriodMillis)
        }

        private fun open(scope: CoroutineScope, cachePeriodMillis: Long, remotePeriodMillis: Long) {

            scope.coroutineContext[Job]?.invokeOnCompletion {
                close()
            }

            scope.launch(Dispatchers.Default) {
                saveChannel.consumeAsFlow()
                    .onEach {
                        lastItem = it
                        lastCacheSave = true
                    }
                    .distinctUntilChanged()
                    .sample(cachePeriodMillis)
                    .onEach {
                        lastItem?.let {
                            save(ONLY_CACHE, false, it)
                        }
                    }
                    .debounce(remotePeriodMillis)
                    .onCompletion {
                        if (lastItem != null) {
                            kotlinx.coroutines.withContext(NonCancellable) {
                                lastItem?.let {
                                    val strategy = if (lastCacheSave) CACHE_AND_REMOTE else ONLY_REMOTE
                                    save(strategy, true, it)
                                    lastItem = null
                                }
                            }
                        }
                    }
                    .collect {
                        lastItem?.let {
                            save(ONLY_REMOTE, true, it)
                            lastItem = null
                        }
                    }
            }
        }


        private suspend fun save(strategy: Strategy, remote: Boolean, item: T) {
            try {
                if (!remote) lastCacheSave = false
                val channel = createChannel(strategy) {
                    block(item)
                }
                channel.receiveOrNull()
            } catch (e: Throwable) {
                errorHandler(remote, e)
            }
        }

        fun skipSave() {
            lastItem = null
        }

        fun post(item: T) {
            saveChannel.offer(item)
        }

        fun close() {
            saveChannel.close()
        }

    }

    class RemoteSource : MainSource {

        override val per: String
            get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

        override fun testMethod() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun testMethodReturn(kek: String?): String {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override val type: com.omega_r.base.data.sources.Source.Type = Source.Type.REMOTE


    }

    class CacheSource : MainSource, com.omega_r.base.data.sources.CacheSource {

        override val per: String
            get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

        override fun testMethod() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun testMethodReturn(kek: String?): String {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun updateItem(data: Any?) {

        }

        override fun clear() {
        }

        override val type: com.omega_r.base.data.sources.Source.Type = Source.Type.FILE_CACHE


    }

}