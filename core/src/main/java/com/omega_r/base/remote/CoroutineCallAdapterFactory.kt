package com.omega_r.base.remote

import com.squareup.moshi.Types.getRawType
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * A [CallAdapter.Factory] for use with Kotlin coroutines.
 *
 * Adding this class to [Retrofit] allows you to return [Deferred] from
 * service methods.
 *
 *     interface MyService {
 *       &#64;GET("user/me")
 *       Deferred&lt;User&gt; getUser()
 *     }
 *
 * There are two configurations supported for the [Deferred] type parameter:
 *
 * * Direct body (e.g., `Deferred<User>`) returns the deserialized body for 2XX responses, throws
 * [HttpException] errors for non-2XX responses, and throws [IOException][java.io.IOException] for
 * network errors.
 * * Response wrapped body (e.g., `Deferred<Response<User>>`) returns a [Response] object for all
 * HTTP responses and throws [IOException][java.io.IOException] for network errors
 */
class CoroutineCallAdapterFactory (private val parent: Job? = null,
                                   private val errorConverter: ((Throwable) -> Exception)? = null) : CallAdapter.Factory() {


    override fun get(returnType: Type, annotations: Array<out Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
        if (Deferred::class.java != getRawType(returnType)) {
            return null
        }
        if (returnType !is ParameterizedType) {
            throw IllegalStateException(
                "Deferred return type must be parameterized as Deferred<Foo> or Deferred<out Foo>")
        }
        val responseType = getParameterUpperBound(0, returnType)

        val rawDeferredType = getRawType(responseType)
        return if (rawDeferredType == Response::class.java) {
            check(responseType is ParameterizedType) { "Response must be parameterized as Response<Foo> or Response<out Foo>" }
            ResponseCallAdapter<Any>(getParameterUpperBound(0, responseType), parent, errorConverter)
        } else {
            BodyCallAdapter<Any>(responseType, parent, errorConverter)
        }
    }

    private class BodyCallAdapter<T>(private val responseType: Type,
                                     private val parent: Job? = null,
                                     private val errorConverter: ((Throwable) -> Exception)? = null) : CallAdapter<T, Deferred<T?>> {

        override fun responseType() = responseType

        override fun adapt(call: Call<T>): Deferred<T?> {
            val deferred = CompletableDeferred<T?>(parent)

            deferred.invokeOnCompletion {
                if (deferred.isCancelled) {
                    call.cancel()
                }
            }

            call.enqueue(object : Callback<T> {
                override fun onFailure(call: Call<T>, t: Throwable) {
                    deferred.completeExceptionally(errorConverter?.invoke(t) ?: t)
                }

                override fun onResponse(call: Call<T>, response: Response<T>) {
                    if (response.isSuccessful) {
                        deferred.complete(response.body())
                    } else {
                        val httpException = HttpException(response)
                        deferred.completeExceptionally(errorConverter?.invoke(httpException) ?: httpException)
                    }
                }
            })

            return deferred
        }
    }

    private class ResponseCallAdapter<T>(private val responseType: Type,
                                         private val parent: Job? = null,
                                         private val errorConverter: ((Throwable) -> Exception)? = null
    ) : CallAdapter<T, Deferred<Response<T>>> {

        override fun responseType() = responseType

        override fun adapt(call: Call<T>): Deferred<Response<T>> {
            val deferred = CompletableDeferred<Response<T>>(parent)

            deferred.invokeOnCompletion {
                if (deferred.isCancelled) {
                    call.cancel()
                }
            }

            call.enqueue(object : Callback<T> {
                override fun onFailure(call: Call<T>, t: Throwable) {
                    deferred.completeExceptionally(errorConverter?.invoke(t) ?: t)
                }

                override fun onResponse(call: Call<T>, response: Response<T>) {
                    deferred.complete(response)
                }
            })

            return deferred
        }
    }
}
