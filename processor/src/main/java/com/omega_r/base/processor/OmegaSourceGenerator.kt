package com.omega_r.base.processor

import com.omega_r.base.processor.Constants.Companion.CHANNEL_CLASS_NAME
import com.omega_r.base.processor.Constants.Companion.CLASS_NAME_STRATEGY
import com.omega_r.base.processor.Constants.Companion.SEND_CHANNEL_CLASS_NAME
import com.omega_r.base.processor.Constants.Companion.UNIT_CLASS_NAME
import com.omega_r.base.processor.extensions.formatType
import com.omega_r.base.processor.extensions.getName
import com.omega_r.base.processor.extensions.isUnitFunction
import com.omega_r.base.processor.extensions.sourceName
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.isSuspend
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf.Function
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import me.eugeniomarletti.kotlin.metadata.shadow.serialization.deserialization.getName
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.lang.model.util.Elements
import javax.tools.Diagnostic

private const val THROW_NO_DATA = "com.omega_r.base.errors.throwNoData()"

internal class OmegaSourceGenerator(
    private val filer: Filer,
    private val messager: Messager,
    private val elements: Elements
) {

    fun generateSource(element: Element) {
        val elementPackage = elements.getPackageOf(element).toString()
        val sourceName = element.sourceName

        val typeSpec = TypeSpec.interfaceBuilder(sourceName)
            .addSuperinterface(Constants.CLASS_NAME_SOURCE)
            .addFunctions(element.generateFunctions())
            .build()

        FileSpec.builder(elementPackage, element.sourceName)
            .addType(typeSpec)
            .build()
            .writeTo(filer)
    }

    private fun Element.generateFunctions(): List<FunSpec> {
        val kotlinMetadata = kotlinMetadata as KotlinClassMetadata

        val classData = kotlinMetadata.data.classProto
        val nameResolver = kotlinMetadata.data.nameResolver

        return classData.functionList.map { function ->
//            function.toFunSpec(nameResolver)
            val functionName = nameResolver.getName(function)
            val functionBuilder = FunSpec.builder(functionName)
                .addParameters(function.generateParameters(nameResolver))

            if (function.isSuspend) functionBuilder.addModifiers(KModifier.SUSPEND)

            val codeBlock = CodeBlock.builder()
            if (function.isUnitFunction(nameResolver)) {
                codeBlock.addStatement("com.omega_r.base.errors.throwNoData()")
            } else {
                val returnType = function.returnType
                val list = returnType.argumentList



                val returnName = nameResolver.getString(returnType.className).formatType()
                val returnClassName = ClassName.bestGuess(returnName)


                if (list.isEmpty()) {
                    functionBuilder.returns(returnClassName)
                    codeBlock.addStatement("return com.omega_r.base.errors.throwNoData()")
                } else {


                    val map = list.map {
                        ClassName.bestGuess(nameResolver.getName(it.type.className).toString().formatType())
                    }

                    val parameterizedTypeName = returnClassName.parameterizedBy(map)
                    functionBuilder.returns(parameterizedTypeName)
                    codeBlock.addStatement("return com.omega_r.base.errors.throwNoData()")
                }

                returnType.argumentList.forEach { argument ->
                    val argumentType = nameResolver.getName(argument.type.className)
                    messager.printMessage(Diagnostic.Kind.WARNING, "argumentType $argumentType")
                }

            }

            functionBuilder
                .addCode(codeBlock.build())
                .build()
        }
    }

    private fun Function.toFunSpec(element: Element, nameResolver: NameResolver): FunSpec {
        val functionName = nameResolver.getName(this)

        val builder = FunSpec.builder(functionName)
            .addParameters(generateParameters(nameResolver))
            .addSuspendIfNeeded(this)

        val codeBlockBuilder = CodeBlock.builder()
        when(returnType.getClassName(nameResolver)) {
            UNIT_CLASS_NAME -> codeBlockBuilder.addStatement(THROW_NO_DATA)
            CHANNEL_CLASS_NAME, SEND_CHANNEL_CLASS_NAME -> {
                messager.printMessage(Diagnostic.Kind.ERROR, "${element.simpleName} contains function " +
                        "$functionName with forbidden return type $returnType")
            }
            else -> {

            }
        }

        return builder
            .addCode(codeBlockBuilder.build())
            .build()
    }

    private fun ProtoBuf.Type.getClassName(nameResolver: NameResolver): ClassName =
        ClassName.bestGuess(nameResolver.getString(className).formatType())

    private fun Function.generateParameters(nameResolver: NameResolver): List<ParameterSpec> {
        return valueParameterList.mapNotNull { parameter ->
            val name = nameResolver.getString(parameter.name)
            val className = ClassName.bestGuess(nameResolver.getString(parameter.type.className).formatType())

            if (className == CLASS_NAME_STRATEGY) return@mapNotNull null
            ParameterSpec(name, className.copy(nullable = parameter.type.nullable))
        }
    }

    private fun FunSpec.Builder.addSuspendIfNeeded(function: Function): FunSpec.Builder {
        if (function.isSuspend) addModifiers(KModifier.SUSPEND)
        return this
    }

}