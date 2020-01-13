package com.omega_r.base.processor.models

import com.squareup.kotlinpoet.ParameterSpec

data class Parameter(
    val name: String,
    val type: Type
) {

    fun toParameterSpec(): ParameterSpec = ParameterSpec(name, type.typeName)

}