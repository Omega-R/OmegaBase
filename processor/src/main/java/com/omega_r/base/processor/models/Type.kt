package com.omega_r.base.processor.models

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName

data class Type(
    val className: ClassName,
    val parameterizedBy: List<Type>,
    val isNullable: Boolean
) {

    val typeName: TypeName
        get() {
            return if (parameterizedBy.isEmpty()) {
                className.copy(isNullable)
            } else {
                className.parameterizedBy(parameterizedBy.map { it.typeName }).copy(isNullable)
            }
        }

}