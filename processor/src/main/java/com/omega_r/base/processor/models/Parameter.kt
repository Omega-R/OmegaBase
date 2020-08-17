package com.omega_r.base.processor.models

import com.omega_r.base.processor.Constants.Companion.MEMBER_NAME_THROW_NO_DATA
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec

data class Parameter(
    val name: String,
    val type: Type
) {

    fun toParameterSpec(): ParameterSpec = ParameterSpec(name, type.typeName)

    fun toPropertySpec(): PropertySpec = PropertySpec.builder(name, type.typeName)
        .getter(
            FunSpec.getterBuilder()
                .addStatement("return %M()", MEMBER_NAME_THROW_NO_DATA)
                .build()
        )
        .build()

}