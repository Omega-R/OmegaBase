package com.omega_r.base.enitity

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

fun <T : Identifiable<I>, I> Iterable<T>.contains(id: I): Boolean {
    return firstOrNull(predicate = { it.id == id }) != null
}

fun <T : Identifiable<I>, I> Iterable<T>.indexOfFirst(id: I): Int {
    return indexOfFirst(predicate = { it.id == id })
}

fun <T : Identifiable<I>, I> Iterable<T>.indexOfLast(id: I): Int {
    return indexOfLast(predicate = { it.id == id })
}

fun <T : Identifiable<I>, I> Iterable<T>.firstOrNull(id: I): T? {
    return firstOrNull(predicate = { it.id == id })
}