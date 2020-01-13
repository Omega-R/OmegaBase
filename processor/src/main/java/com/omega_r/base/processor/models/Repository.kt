package com.omega_r.base.processor.models

import com.omega_r.base.processor.Constants.Companion.CLASS_NAME_ERROR_HANDLER
import com.omega_r.base.processor.Constants.Companion.CLASS_NAME_OMEGA_REPOSITORY
import com.omega_r.base.processor.Constants.Companion.CLASS_NAME_STRATEGY
import com.omega_r.base.processor.Constants.Companion.MEMBER_NAME_CONSUME_EACH
import com.omega_r.base.processor.Constants.Companion.MEMBER_RUN_BLOCKING
import com.omega_r.base.processor.Constants.Companion.RECEIVE_CHANNEL_CLASS_NAME
import com.omega_r.base.processor.Constants.Companion.REMOTE_ELSE_CACHE
import com.omega_r.base.processor.Constants.Companion.UNIT_CLASS_NAME
import com.omega_r.base.processor.extensions.removeChannelSuffix
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import me.eugeniomarletti.kotlin.metadata.shadow.util.capitalizeDecapitalize.decapitalizeAsciiOnly

class Repository(
    val repositoryPackage: String,
    val name: String,
    val superInterfaceClassName: ClassName,
    val functions: List<Function>
) {

    fun toFileSpec(source: Source): FileSpec {
        val typeSpec = TypeSpec.classBuilder(name)
            .addConstructor(source)
            .addFunctions(functions.map { it.withImplementation() })
            .build()

        return FileSpec.builder(repositoryPackage, name)
            .addType(typeSpec)
            .build()
    }

    private fun TypeSpec.Builder.addConstructor(source: Source): TypeSpec.Builder {
        val errorHandlerName = CLASS_NAME_ERROR_HANDLER.simpleName.decapitalizeAsciiOnly()
        val sourcesName = source.name.decapitalizeAsciiOnly()

        return superclass(CLASS_NAME_OMEGA_REPOSITORY.parameterizedBy(source.className))
            .addModifiers(KModifier.OPEN)
            .addSuperclassConstructorParameter("$errorHandlerName, *$sourcesName")
            .addSuperinterface(superInterfaceClassName)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(errorHandlerName, CLASS_NAME_ERROR_HANDLER)
                    .addParameter(sourcesName, source.className, KModifier.VARARG)
                    .build()
            )
    }

    private fun Function.withImplementation(): FunSpec {
        return toFunSpec().toBuilder()
            .addModifiers(KModifier.OVERRIDE)
            .addCode(getCodeBody())
            .build()
    }

    private fun Function.getCodeBody(): CodeBlock {
        val funcWithArguments = name.removeChannelSuffix() + getArguments()
        val strategy = getStrategy()

        val codeBlockBuilder = CodeBlock.builder()
        when (this.returnType?.className) {
            null, UNIT_CLASS_NAME -> {
                codeBlockBuilder.addStatement("createChannel($strategy) { $funcWithArguments }.%M{}", MEMBER_NAME_CONSUME_EACH)
            }
            RECEIVE_CHANNEL_CLASS_NAME -> codeBlockBuilder.add("return createChannel($strategy) {  $funcWithArguments } \n")
            else -> {
                if (modifiers.contains(KModifier.SUSPEND)) {
                    codeBlockBuilder.add("return createChannel($strategy) { $funcWithArguments }.receive()\n")
                } else {
                    codeBlockBuilder.addStatement(
                        "return %M { createChannel($strategy) { $funcWithArguments }.receive() }",
                        MEMBER_RUN_BLOCKING
                    )
                }
            }
        }
        return codeBlockBuilder.build()
    }

    private fun Function.getStrategy(): String =
        parameters.firstOrNull { it.type.className == CLASS_NAME_STRATEGY }?.name ?: REMOTE_ELSE_CACHE

    private fun Function.getArguments(): String {
        return parameters.filter {
            it.type.className != CLASS_NAME_STRATEGY
        }.joinToString(prefix = "(", postfix = ")") { it.name }
    }

}