package com.omega_r.base.crash

import android.app.Activity
import android.content.Context
import com.omega_r.libs.omegaintentbuilder.OmegaIntentBuilder
import java.io.PrintWriter
import java.io.StringWriter

class EmailSenderCrashWay(private val emailTo: String? = null) : CrashSender.SenderCrashWay {

    override fun send(context: Context, currentActivity: Activity?, error: Throwable, crashReport: CrashReport) {
        OmegaIntentBuilder.share()
            .subject("~ CRASH REPORT ~")
            .text(createBodyText(crashReport))
            .apply {
                crashReport.screenshot?.let { bitmap(crashReport.screenshot) }
                emailTo?.let { emailTo(emailTo) }
            }
            .startActivity(context)
    }

    private fun createBodyText(crashReport: CrashReport): String {
        val stringWriter = StringWriter()
        with(PrintWriter(stringWriter)) {
            printGroupMap(crashReport.info)
            printGroupSingleValue("Stack Trace", crashReport.stacktrace)
        }
        return stringWriter.toString()
    }


    private fun PrintWriter.printGroupMap(map: Map<String, Map<String, String>>) {
        map.forEach {
            println("[${it.key}]: ")
            printMap(it.value)
            println()
        }
    }

    private fun PrintWriter.printGroupSingleValue(groupName: String, value: String) {
        if (value.isNotEmpty()) {
            println("[$groupName]: ")
            println(value)
            println()
        }
    }

    private fun PrintWriter.printMap(map: Map<String, String>) {
        map.forEach {
            println(" - ${it.key}: ${it.value}")
        }
    }

}