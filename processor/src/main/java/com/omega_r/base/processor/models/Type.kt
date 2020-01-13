package com.omega_r.base.processor.models

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

data class Type(
    val className: ClassName,
    val parameterizedBy: List<Type>
) {

    val typeName: TypeName = if (parameterizedBy.isEmpty()) className else className.parameterizedBy(parameterizedBy.map { it.typeName })

}