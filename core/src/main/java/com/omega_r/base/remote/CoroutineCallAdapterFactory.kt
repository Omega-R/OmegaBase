package com.omega_r.base.remote

import com.omega_r.libs.extensions.common.ifNull
import okhttp3.Request
import retrofit2.*
import java.lang.reflect.Type

class CoroutineCallAdapterFactory(private val errorConverter: ((Throwable) -> Exception)? = null) : CallAdapter.Factory() {

    override fun get(returnType: Type, annotations: Array<Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
        val callAdapter = retrofit.nextCallAdapter(this, returnType, annotations)
        return CallAdapterWrapper(callAdapter)
    }

    inner class CallAdapterWrapper<R, T>(private val adapter: CallAdapter<R, T>) : CallAdapter<R, T> {

        override fun adapt(call: Call<R>): T = adapter.adapt(CallWrapper(call))

        override fun responseType(): Type = adapter.responseType()

    }

    inner class CallWrapper<R>(private val delegate: Call<R>) : Call<R> {

        override fun enqueue(callback: Callback<R>) {
            delegate.enqueue(object : Callback<R> {

                override fun onFailure(call: Call<R>, t: Throwable) {
                    callback.onFailure(call, errorConverter?.invoke(t) ?: t)
                }

                override fun onResponse(call: Call<R>, response: Response<R>) {
                    if (response.isSuccessful) {
                        callback.onResponse(call, response)
                    } else {
                        errorConverter?.invoke(HttpException(response))?.let {
                            callback.onFailure(call, it)
                        }.ifNull {
                            callback.onResponse(call, response)
                        }
                    }
                }
            })

        }

        override fun isExecuted(): Boolean = delegate.isExecuted

        override fun clone(): Call<R> = CallWrapper(delegate.clone())

        override fun isCanceled(): Boolean = delegate.isCanceled

        override fun cancel() {
            delegate.cancel()
        }

        override fun execute(): Response<R> = delegate.execute()

        override fun request(): Request = delegate.request()

    }

}