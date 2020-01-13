package com.omega_r.base.processor.models

import com.omega_r.base.processor.Constants
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec

private const val THROW_NO_DATA = "com.omega_r.base.errors.throwNoData()"

class Source(
    val sourcePackage: String,
    val name: String,
    val functions: List<Function>
) {

    val className: ClassName = ClassName.bestGuess("$sourcePackage.$name")

    fun toFileSpec(): FileSpec {
        val funcSpecs = functions.map { function ->
            val codeBlock = CodeBlock.builder()

            codeBlock.addStatement(
                when (function.returnType?.className) {
                    null, Constants.UNIT_CLASS_NAME -> THROW_NO_DATA
                    else -> "return $THROW_NO_DATA"
                }
            )

            function.toFunSpec()
                .toBuilder()
                .addCode(codeBlock.build())
                .build()
        }

        val typeSpec = TypeSpec.interfaceBuilder(name)
            .addSuperinterface(Constants.CLASS_NAME_SOURCE)
            .addFunctions(funcSpecs)
            .build()

        return FileSpec.builder(sourcePackage, name)
            .addType(typeSpec)
            .build()
    }

}

