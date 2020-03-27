package com.omega_r.base.processor.factories

import com.omega_r.base.processor.Constants.Companion.CHANNEL_CLASS_NAME
import com.omega_r.base.processor.Constants.Companion.SEND_CHANNEL_CLASS_NAME
import com.omega_r.base.processor.Constants.Companion.UNIT_CLASS_NAME
import com.omega_r.base.processor.extensions.*
import com.omega_r.base.processor.models.Function
import com.omega_r.base.processor.models.Parameter
import com.omega_r.base.processor.models.Repository
import com.omega_r.base.processor.models.Type
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import me.eugeniomarletti.kotlin.metadata.*
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf.Class.Kind.INTERFACE
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.tools.Diagnostic.Kind.ERROR

private const val FUNC_CLEAR_CACHE = "clearCache"

class RepositoryFactory(private val messager: Messager, private val elements: Elements) {

    fun create(elements: Set<Element>): List<Repository> = elements.mapNotNull { create(it) }

    fun create(element: Element): Repository? {
        val kotlinMetadata = element.kotlinMetadata

        if (element !is TypeElement || kotlinMetadata !is KotlinClassMetadata) {
            messager.printMessage(ERROR, "@AppOmegaRepository can't be applied to $element: must be a Kotlin interface")
            return null
        }

        val classData: ClassData = kotlinMetadata.data
        if (classData.classProto.classKind != INTERFACE) {
            messager.printMessage(ERROR, "@AppOmegaRepository can't be applied to $element: must be a Kotlin interface")
            return null
        }

        val functions = classData.getFunctions(element).toMutableList()
        element.interfaces.forEach {
            functions += create(it.asTypeElement())?.functions ?: return@forEach
        }

        return Repository(
            element.repositoryPackage,
            element.repositoryName,
            element.superInterfaceClassName,
            classData.getParameters(),
            functions
        )
    }

    private val Element.repositoryPackage
        get() = elements.packageOf(this)

    private val Element.superInterfaceClassName
        get() = ClassName.bestGuess("${elements.packageOf(this)}.${this.simpleName}")

    private fun ClassData.getParameters(): List<Parameter> {
        return classProto.propertyList.mapNotNull { property ->
            property.toParameter(nameResolver)
        }
    }

    private fun ProtoBuf.Property.toParameter(nameResolver: NameResolver): Parameter? {
        if (modality != ProtoBuf.Modality.ABSTRACT) return null
        val parameterName = nameResolver.getString(name)
        val className = returnType.getClassName(nameResolver)
        val parameterizedBy = getParameterTypes(returnType, nameResolver)

        return Parameter(parameterName, Type(className, parameterizedBy, returnType.nullable))
    }

    private fun ClassData.getFunctions(element: Element): List<Function> {
        return classProto.functionList.mapNotNull { function ->
            function.toFunction(element, nameResolver)
        }
    }

    private fun ProtoBuf.Function.toFunction(element: Element, nameResolver: NameResolver): Function? {
        if (modality != ProtoBuf.Modality.ABSTRACT) return null

        val functionName = nameResolver.getName(this)
        if (functionName == FUNC_CLEAR_CACHE) return null

        val parameters = valueParameterList.map {
            it.toParameter(nameResolver)
        }.toLinkedHashSet()
        val modifiers = if (isSuspend) listOf(KModifier.SUSPEND) else emptyList()

        when (returnType.getClassName(nameResolver)) {
            CHANNEL_CLASS_NAME, SEND_CHANNEL_CLASS_NAME -> {
                messager.printMessage(
                    ERROR, "${element.simpleName} contains function $functionName with forbidden return type $returnType"
                )
                return null
            }
        }
        val returnType = getReturnType(nameResolver)

        return Function(functionName, parameters, modifiers.toLinkedHashSet(), returnType)
    }

    private fun ProtoBuf.ValueParameter.toParameter(nameResolver: NameResolver): Parameter {
        val parameterName = nameResolver.getString(name)
        val className = type.getClassName(nameResolver)
        val parameterizedBy = getParameterTypes(type, nameResolver)

        return Parameter(parameterName, Type(className, parameterizedBy, type.nullable))
    }

    private fun ProtoBuf.Type.getClassName(nameResolver: NameResolver): ClassName =
        ClassName.bestGuess(nameResolver.getClassName(className))

    private fun getParameterTypes(type: ProtoBuf.Type, nameResolver: NameResolver): List<Type> {
        val arguments = type.argumentList

        return if (arguments.isEmpty()) {
            emptyList()
        } else {
            arguments.flatMap {
                listOf(Type(it.type.getClassName(nameResolver), getParameterTypes(it.type, nameResolver), it.type.nullable))
            }
        }
    }

    private fun ProtoBuf.Function.getReturnType(nameResolver: NameResolver): Type? {
        return when (val className = returnType.getClassName(nameResolver)) {
            UNIT_CLASS_NAME -> null
            else -> Type(className, getParameterTypes(returnType, nameResolver), returnType.nullable)
        }
    }

}