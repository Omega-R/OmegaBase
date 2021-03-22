package com.omega_r.base.crash

import android.app.Activity
import android.content.Context
import android.util.Log
import okhttp3.*
import org.json.JSONObject
import java.io.IOException


class RocketChatSenderCrashWay(private val webHookUrl: String) : CrashSender.SenderCrashWay {

    companion object {
        private val MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8")
    }

    private val client = OkHttpClient()

    override fun send(context: Context, currentActivity: Activity?, error: Throwable, crashReport: CrashReport) {
        val content = """
        {
            "text": ${JSONObject.quote("```\n" + crashReport.stacktrace + "\n```")},
            ${getJsonAttachments(crashReport.info)}
            
        }
        """.trimIndent()
        val body = RequestBody.create(
            MEDIA_TYPE_JSON,
            content
        )

        val request: Request = Request.Builder()
            .post(body)
            .url(webHookUrl)
            .build()


        client.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                // nothing
            }

        })

        Thread.sleep(1)

    }

    private fun getJsonFields(map: Map<String, String>): String {
        val entries = map.entries
        return StringBuilder().apply {
            append("\"fields\":[")
            entries.forEachIndexed { index, entry ->
                if (index > 0) {
                    append(",")
                }
                append("{")
                append("\"title\": ${JSONObject.quote(entry.key)}, ")
                append("\"value\": ${JSONObject.quote(entry.value)}")
                append("}")


            }
            append("]")
        }.toString()
    }

    private fun getJsonAttachments(map: Map<String, Map<String, String>>): String {
        val entries = map.entries
        return StringBuilder().apply {
            append("\"attachments\":[")
            entries.forEachIndexed { index, entry ->
                if (index > 0) {
                    append(",")
                }
                append("{")
                append("\"title\": ${JSONObject.quote(entry.key)}, ")
                append(getJsonFields(entry.value))
                append("}")

            }
            append("]")
        }.toString()

    }

}