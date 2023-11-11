package com.omega_r.base.remote.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

private const val HEADER_USER_AGENT = "User-Agent"

class UserAgentInterceptor(private val userAgent: String) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestWithUserAgent = originalRequest.newBuilder()
            .header(HEADER_USER_AGENT, userAgent)
            .build()
        return chain.proceed(requestWithUserAgent)
    }
}