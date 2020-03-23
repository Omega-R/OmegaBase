package com.omega_r.base.processor.models

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier

data class Function (
    val name: String,
    val parameters: Set<Parameter>,
    val modifiers: Set<KModifier>,
    val returnType: Type? = null
) {

    fun toFunSpec(): FunSpec {
        val builder = FunSpec.builder(name)
            .addParameters(parameters.map { it.toParameterSpec() })
            .addModifiers(modifiers)

        returnType?.let {
            builder.returns(it.typeName)
        }

        return builder.build()
    }

}