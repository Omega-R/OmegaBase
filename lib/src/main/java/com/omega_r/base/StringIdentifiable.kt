package com.omega_r.base

/**
 * Created by Anton Knyazev on 28.04.2019.
 */
interface StringIdentifiable : Identifiable {

    val idAsString: String

    override val id: Long
        get() = idAsString.hashCode().toLong()

}