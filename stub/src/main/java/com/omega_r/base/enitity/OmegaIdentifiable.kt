package com.omega_r.adapters

interface OmegaIdentifiable<T> {

    val id: T

    val idAsLong: Long
        get() = 0

}