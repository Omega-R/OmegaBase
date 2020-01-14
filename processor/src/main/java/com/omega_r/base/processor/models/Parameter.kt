package com.omega_r.base.processor.models

import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec

data class Parameter(
    val name: String,
    val type: Type
) {

    fun toParameterSpec(): ParameterSpec = ParameterSpec(name, type.typeName)

    fun toPropertySpec(): PropertySpec = PropertySpec.builder(name, type.typeName).build()

}