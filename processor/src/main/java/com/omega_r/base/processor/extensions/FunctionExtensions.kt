package com.omega_r.base.processor.extensions


import com.omega_r.base.processor.Constants.Companion.UNIT
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf.Function
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver

fun NameResolver.getName(function: Function): String = getString(function.name)

fun Function.isUnitFunction(resolver: NameResolver) =
    resolver.getString(returnType.className).formatType() == UNIT

fun String.formatType(): String = replace("/", ".")