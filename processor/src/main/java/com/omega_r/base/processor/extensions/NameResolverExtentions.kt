package com.omega_r.base.processor.extensions

import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver

fun NameResolver.getClassName(index: Int): String = getString(index).formatType()