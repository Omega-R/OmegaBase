package com.omega_r.base.processor.extensions

fun <T> Collection<T>.toLinkedHashSet(): LinkedHashSet<T> {
    return if (this is LinkedHashSet) this else LinkedHashSet(this)
}