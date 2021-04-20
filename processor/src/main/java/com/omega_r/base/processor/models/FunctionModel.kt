package com.omega_r.base.processor.models

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier

data class FunctionModel (
    val name: String,
    val parameters: Set<ParameterModel>,
    val modifiers: Set<KModifier>,
    val returnType: TypeModel? = null
) {


}