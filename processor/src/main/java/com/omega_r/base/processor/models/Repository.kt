package com.omega_r.base.processor.models

import com.omega_r.base.processor.Constants.Companion.CLASS_NAME_ERROR_HANDLER
import com.omega_r.base.processor.Constants.Companion.CLASS_NAME_OMEGA_REPOSITORY
import com.omega_r.base.processor.Constants.Companion.CLASS_NAME_STRATEGY
import com.omega_r.base.processor.Constants.Companion.MEMBER_NAME_CONSUME_EACH
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
    val properties: List<Parameter>,
    val functions: List<Function>
) {

    fun toFileSpec(source: Source): FileSpec {
        val typeSpec = TypeSpec.classBuilder(name)
            .addConstructor(source)
            .addFunctions(functions.mapNotNull { it.withImplementation() })
            .build()

        return FileSpec.builder(repositoryPackage, name)
            .addType(typeSpec)
            .build()
    }

    private fun TypeSpec.Builder.addConstructor(source: Source): TypeSpec.Builder {
        val errorHandlerName = CLASS_NAME_ERROR_HANDLER.simpleName.decapitalizeAsciiOnly()
        val sourcesName = source.name.decapitalizeAsciiOnly()

        return superclass(CLASS_NAME_OMEGA_REPOSITORY.parameterizedBy(source.className))
            .addModifiers(generateModifier())
            .addSuperclassConstructorParameter("$errorHandlerName, *$sourcesName")
            .addSuperinterface(superInterfaceClassName)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(errorHandlerName, CLASS_NAME_ERROR_HANDLER)
                    .addParameter(sourcesName, source.className, KModifier.VARARG)
                    .build()
            )
    }

    private fun generateModifier(): KModifier {
        return functions.firstOrNull {
            !it.modifiers.contains(KModifier.SUSPEND) && it.returnType?.className != RECEIVE_CHANNEL_CLASS_NAME
        }?.let {
            KModifier.ABSTRACT
        } ?: if (properties.isEmpty()) KModifier.OPEN else KModifier.ABSTRACT
    }

    private fun Function.withImplementation(): FunSpec? {
        if (!modifiers.contains(KModifier.SUSPEND) && returnType?.className != RECEIVE_CHANNEL_CLASS_NAME) {
            return null
        }

        return toFunSpec().toBuilder()
            .addModifiers(KModifier.OVERRIDE)
            .addCode(getCodeBody())
            .build()
    }

    private fun Function.getCodeBody(): CodeBlock {
        val funcWithArguments = name.removeChannelSuffix() + getArguments()
        val strategy = getStrategy()

        return CodeBlock.builder().apply {
            when (returnType?.className) {
                null, UNIT_CLASS_NAME -> {
                    addStatement("createChannel($strategy) { $funcWithArguments }.%M{}", MEMBER_NAME_CONSUME_EACH)
                }
                RECEIVE_CHANNEL_CLASS_NAME -> add("return createChannel($strategy) {  $funcWithArguments } \n")
                else -> add("return createChannel($strategy) { $funcWithArguments }.receive()\n")
            }
        }.build()
    }

    private fun Function.getStrategy(): String =
        parameters.firstOrNull { it.type.className == CLASS_NAME_STRATEGY }?.name ?: REMOTE_ELSE_CACHE

    private fun Function.getArguments(): String {
        return parameters.filter {
            it.type.className != CLASS_NAME_STRATEGY
        }.joinToString(prefix = "(", postfix = ")") { it.name }
    }

}