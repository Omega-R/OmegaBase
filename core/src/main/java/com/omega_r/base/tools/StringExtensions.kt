package com.omega_r.base.tools

/**
 * Created by Anton Knyazev on 30.04.19.
 */
fun String?.toLongHash(): Long {
    val string = this ?: ""

    var h = 98764321261L
    val l = string.length
    val chars = string.toCharArray()

    for (i in 0 until l) {
        h = 31 * h + chars[i].toLong()
    }
    return h
}
