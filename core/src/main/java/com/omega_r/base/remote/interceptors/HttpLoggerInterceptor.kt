package com.omega_r.base.remote.interceptors

import com.omega_r.base.logs.LogManager
import com.omega_r.base.logs.Logger
import okhttp3.Interceptor
import okhttp3.Interceptor.Chain
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import okhttp3.logging.HttpLoggingInterceptor.Level.NONE
import java.nio.charset.Charset
import java.nio.charset.CharsetEncoder

class HttpLoggingInterceptor(enabled: Boolean) : Interceptor {

    private val originalInterceptor = HttpLoggingInterceptor(HttpLogger)

    var enabled: Boolean
        get() = originalInterceptor.level == BODY
        set(value) {
            originalInterceptor.level = if (value) BODY else NONE
        }

    init {
        this.enabled = enabled
    }

    override fun intercept(chain: Chain) = originalInterceptor.intercept(chain)

    private object HttpLogger : HttpLoggingInterceptor.Logger {

        private val encoder = object:ThreadLocal<CharsetEncoder>() {
            override fun initialValue() = Charset.forName("ISO-8859-1").newEncoder()
        }

        private const val TAG = "OkHttp"
        private const val MESSAGE_BINARY = "<BINARY DATA>"

        override fun log(message: String) {
            val maxLogLength = 4000



            // Split by line, then ensure each line can fit into Log's maximum length.
            var i = 0
            val length = message.length
            var isBinaryLogDisplayed = false
            var isBinaryContentType = false
            while (i < length) {
                var newline = message.indexOf('\n', i)
                newline = if (newline != -1) newline else length
                do {
                    val end = minOf(newline, i + maxLogLength)
                    val msg = message.substring(i, end).trim()

                    if (msg.contains("Content-Type") && msg.contains("application/octet-stream")) { // use another Content-Type if need
                        isBinaryContentType = true
                    }
                    val isBinaryData = encoder.get()?.canEncode(msg) == false

                    // multipart boundary
                    if (isBinaryLogDisplayed && msg.startsWith("--")) {
                        isBinaryContentType = false
                        isBinaryLogDisplayed = false
                    }

                    // don't print binary data
                    if (isBinaryContentType && isBinaryData && !isBinaryLogDisplayed) {
                        MESSAGE_BINARY.logMessage()
                        isBinaryLogDisplayed = true
                    }

                    if (!isBinaryLogDisplayed) {
                        msg.logMessage()
                    }

                    i = end
                } while (i < newline)
                i++
            }
        }

        private fun String.logMessage() {
            LogManager.log(
                level = Logger.Level.DEBUG,
                tag = TAG,
                message = this
            )
        }

    }
}