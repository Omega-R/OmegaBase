package com.omega_r.base.processor.models

import com.omega_r.base.processor.Constants
import com.omega_r.base.processor.Constants.Companion.MEMBER_NAME_THROW_NO_DATA
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import javax.lang.model.element.Element

class SourceModel(
    val sourcePackage: String,
    val name: String,
    val properties: List<ParameterModel>,
    val functions: List<FunctionModel>,
    val originatingElement: Element
) {

    val className: ClassName = ClassName.bestGuess("$sourcePackage.$name")

}