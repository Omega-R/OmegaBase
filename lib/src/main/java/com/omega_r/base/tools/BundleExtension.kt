package com.omega_r.base.tools

import android.os.Bundle

/**
 * Created by Anton Knyazev on 30.04.19.
 */
fun Bundle?.equalsBundle(other: Bundle?): Boolean {
    if (this != null && other != null) {

        if (size() != other.size())
            return false

        if (!keySet().containsAll(other.keySet()))
            return false

        for (key in keySet()) {
            val valueOne = get(key)
            val valueTwo = other.get(key)
            if (valueOne is Bundle && valueTwo is Bundle) {
                if (!valueOne.equalsBundle(valueTwo)) return false
            } else if (valueOne != valueTwo) return false

        }
        return true

    } else return this == null && other == null
}

fun Bundle.hashCodeBundle(): Int {
    var result = 0
    for (key in keySet()) {
        val value = get(key)
        result = 31 * result +  when (value) {
            is Bundle -> value.hashCodeBundle()
            else -> value?.hashCode() ?: 0
        }
    }
    return result
}