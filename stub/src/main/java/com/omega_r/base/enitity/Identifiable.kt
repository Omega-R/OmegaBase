package com.omega_r.base.enitity

interface Identifiable<T> {

    val id: T

    val idAsLong: Long
        get() = 0

}