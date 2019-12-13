package com.omega_r.base.simple

import android.os.SystemClock
import com.omega_r.base.data.OmegaRepository
import com.omega_r.base.data.OmegaRepository.Strategy.*
import com.omega_r.base.data.sources.Source
import com.omega_r.base.errors.ErrorHandler
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.receiveOrNull
import kotlinx.coroutines.flow.*
import kotlin.coroutines.coroutineContext
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class TestRepository(errorHandler: ErrorHandler) :
    OmegaRepository<MainSource>(errorHandler, RemoteSource(), CacheSource()) {

    fun testMethodReturn(strategy: OmegaRepository.Strategy, kek: String): ReceiveChannel<String> {
        return createChannel(strategy) { testMethodReturn(kek) }
    }

    fun <T, R : CoroutineScope> createRepositoryProperty(
        block: suspend MainSource.(item: T) -> Unit
    ): RepositoryProperty<T, R> {
        return RepositoryProperty(block)
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