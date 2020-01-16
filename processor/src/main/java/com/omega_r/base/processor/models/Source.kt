package com.omega_r.base.processor.models

import com.omega_r.base.processor.Constants
import com.omega_r.base.processor.Constants.Companion.MEMBER_NAME_THROW_NO_DATA
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec

class Source(
    val sourcePackage: String,
    val name: String,
    val properties: List<Parameter>,
    val functions: List<Function>
) {

    val className: ClassName = ClassName.bestGuess("$sourcePackage.$name")

    fun toFileSpec(): FileSpec {
        val funcSpecs = functions.map { function ->
            val codeBlock = CodeBlock.builder()

            codeBlock.addStatement(
                when (function.returnType?.className) {
                    null, Constants.UNIT_CLASS_NAME -> "%M()"
                    else -> "return %M()"
                }
            , MEMBER_NAME_THROW_NO_DATA)

            function.toFunSpec()
                .toBuilder()
                .addCode(codeBlock.build())
                .build()
        }

        val typeSpec = TypeSpec.interfaceBuilder(name)
            .addSuperinterface(Constants.CLASS_NAME_SOURCE)
            .addFunctions(funcSpecs)
            .addProperties(properties.map { it.toPropertySpec() })
            .build()

        return FileSpec.builder(sourcePackage, name)
            .addType(typeSpec)
            .build()
    }

}