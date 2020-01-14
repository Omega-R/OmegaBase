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
import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.classKind
import me.eugeniomarletti.kotlin.metadata.isSuspend
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf.Class.Kind.INTERFACE
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import javax.annotation.processing.Messager
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.tools.Diagnostic.Kind.ERROR

class RepositoryFactory(private val messager: Messager, private val elements: Elements) {

    fun create(elements: Set<Element>): List<Repository> = elements.mapNotNull { create(it) }

    fun create(element: Element): Repository? {
        val kotlinMetadata = element.kotlinMetadata

        if (element !is TypeElement || kotlinMetadata !is KotlinClassMetadata) {
            messager.printMessage(ERROR, "@AppOmegaRepository can't be applied to $element: must be a Kotlin interface")
            return null
        }

        val proto = kotlinMetadata.data.classProto
        if (proto.classKind != INTERFACE) {
            messager.printMessage(ERROR, "@AppOmegaRepository can't be applied to $element: must be a Kotlin interface")
            return null
        }

        val repositoryPackage = elements.packageOf(element)
        val repositoryName = element.repositoryName

        val classProto = kotlinMetadata.data.classProto
        val nameResolver = kotlinMetadata.data.nameResolver
        val superInterfaceClassName = ClassName.bestGuess("${elements.packageOf(element)}.${element.simpleName}")

        val properties = classProto.propertyList.map { property ->
            property.toParameter(nameResolver)
        }
        val functions = classProto.functionList.mapNotNull {
            it.toFunction(element, nameResolver)
        }

        return Repository(repositoryPackage, repositoryName, superInterfaceClassName, properties, functions)
    }

    private fun ProtoBuf.Property.toParameter(nameResolver: NameResolver): Parameter {
        val parameterName = nameResolver.getString(name)
        val className = returnType.getClassName(nameResolver)
        val parameterizedBy = getParameterTypes(returnType, nameResolver)

        return Parameter(parameterName, Type(className, parameterizedBy, returnType.nullable))
    }

    private fun ProtoBuf.Function.toFunction(element: Element, nameResolver: NameResolver): Function? {
        val functionName = nameResolver.getName(this)
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
                val list = getParameterTypes(it.type, nameResolver)
                if (list.isEmpty()) {
                    listOf(Type(it.type.getClassName(nameResolver), emptyList(), it.type.nullable))
                } else {
                    list
                }
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