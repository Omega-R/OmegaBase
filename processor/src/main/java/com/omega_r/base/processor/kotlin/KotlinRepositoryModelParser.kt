package com.omega_r.base.processor.kotlin

import com.omega_r.base.processor.Constants.Companion.CHANNEL_CLASS_NAME
import com.omega_r.base.processor.Constants.Companion.SEND_CHANNEL_CLASS_NAME
import com.omega_r.base.processor.Constants.Companion.UNIT_CLASS_NAME
import com.omega_r.base.processor.extensions.*
import com.omega_r.base.processor.parser.RepositoryModelParser
import com.omega_r.base.processor.models.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.metadata.*
import kotlinx.metadata.*
import kotlinx.metadata.jvm.KotlinClassMetadata
import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.tools.Diagnostic.Kind.ERROR


private const val FUNC_CLEAR_CACHE = "clearCache"

@KotlinPoetMetadataPreview
class KotlinRepositoryModelParser(private val messager: Messager, private val elements: Elements):
    RepositoryModelParser {

    private val Element.repositoryPackage
        get() = elements.packageOf(this)

    private val Element.superInterfaceClassName
        get() = ClassName.bestGuess("${elements.packageOf(this)}.${this.simpleName}")

    override fun parse(element: Element): RepositoryModel? {
        val kotlinMetadata = element.kotlinMetadata

        if (element !is TypeElement || kotlinMetadata !is KotlinClassMetadata.Class) {
            messager.printMessage(ERROR, "@AppOmegaRepository can't be applied to $element: must be a Kotlin interface")
            return null
        }

        val classData = kotlinMetadata.toKmClass()
        if (!classData.isInterface) {
            messager.printMessage(ERROR, "@AppOmegaRepository can't be applied to $element: must be a Kotlin interface")
            return null
        }

        val functions = classData.functionModels.toMutableList()
        val parameters = classData.parameterModels.toMutableList()
        element.interfaces.forEach {
            val parentElement = parse(it.asTypeElement()) ?: return@forEach
            functions += parentElement.functions
            parameters += parentElement.properties
        }

        return RepositoryModel(
            element.repositoryPackage,
            element.repositoryName,
            element.superInterfaceClassName,
            parameters,
            functions,
            element
        )
    }

    private val KmClass.parameterModels: List<ParameterModel>
        get() = properties.mapNotNull { it.toParameterModel() }

    private val KmTypeParameter.parameterModel: ParameterModel?
        get() {
            if (flags.isAbstract) return null
            val parameterName = name
            val type = this.upperBounds.first()
            val className = type.className
            val parameterizedBy = type.parameterTypeModels

            return ParameterModel(parameterName, TypeModel(className, parameterizedBy, type.isNullable))
        }

    private val KmClass.functionModels: List<FunctionModel>
        get() {
            return functions.mapNotNull { function ->
                function.toFunctionModel(this)
            }
        }

    private fun KmFunction.toFunctionModel(clazz: KmClass): FunctionModel? {
        if (!flags.isAbstract || name == FUNC_CLEAR_CACHE) return null

        val parameters = this.valueParameters.map {
            it.toParameterModel()
        }.toLinkedHashSet()

        val modifiers = if (isSuspend) listOf(KModifier.SUSPEND) else emptyList()

        when (returnType.className) {
            CHANNEL_CLASS_NAME, SEND_CHANNEL_CLASS_NAME -> {
                messager.printMessage(
                    ERROR, "${clazz.name} contains function $name with forbidden return type $returnType"
                )
                return null
            }
        }
        val returnType = returnTypeModel

        return FunctionModel(name, parameters, modifiers.toLinkedHashSet(), returnType)
    }

    private fun KmProperty.toParameterModel(): ParameterModel? {
        val type = returnType
        val className = type.className

        val parameterizedBy = type.parameterTypeModels

        return ParameterModel(name, TypeModel(className, parameterizedBy, type.isNullable))
    }

    private fun KmValueParameter.toParameterModel(): ParameterModel {
        val type = type!!
        val className = type.className

        val parameterizedBy = type.parameterTypeModels

        return ParameterModel(name, TypeModel(className, parameterizedBy, type.isNullable))
    }

    private val KmType.className: ClassName
        get() = ClassName.bestGuess((classifier as KmClassifier.Class).name.replace('/', '.'))

    private val KmType.parameterTypeModels: List<TypeModel>
        get() {
            val arguments = arguments

            return if (arguments.isEmpty()) {
                emptyList()
            } else {
                arguments.flatMap {
                    listOf(TypeModel(it.type!!.className, it.type!!.parameterTypeModels, it.type!!.isNullable))
                }
            }
        }

    private val KmFunction.returnTypeModel: TypeModel?
        get() {
            return when (val className = returnType.className) {
                UNIT_CLASS_NAME -> null
                else -> TypeModel(className, returnType.parameterTypeModels, returnType.isNullable)
            }
        }

}

@KotlinPoetMetadataPreview
private val Element.kotlinMetadata: KotlinClassMetadata?
    get() = getAnnotation(Metadata::class.java)?.readKotlinClassMetadata()
