package com.omega_r.base

import com.omega_r.base.tools.toLongHash

/**
 * Created by Anton Knyazev on 13.04.2019.
 */
interface Identifiable<T> {

    val id: T

    val idAsLong: Long
        get() = when (val id = id) {
            is String -> id.toLongHash()
            is Int -> id.toLong()
            is Long -> id
            else -> id.hashCode().toLong()
        }
}
