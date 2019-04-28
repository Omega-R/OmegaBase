package com.omega_r.base

/**
 * Created by Anton Knyazev on 13.04.2019.
 */
interface Identifiable<T> {

    val id: T

    val idAsLong: Long
        get() = id.hashCode().toLong()

}
