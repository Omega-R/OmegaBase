package com.omega_r.base.processor.kotlin

import com.omega_r.base.processor.Constants
import com.omega_r.base.processor.extensions.removeChannelSuffix
import com.omega_r.base.processor.generators.FileGenerator
import com.omega_r.base.processor.models.FunctionModel
import com.omega_r.base.processor.models.ParameterModel
import com.omega_r.base.processor.models.RepositoryModel
import com.omega_r.base.processor.models.SourceModel
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.util.*
import javax.annotation.processing.Filer

class KotlinFileGenerator(private val filer: Filer) : FileGenerator {

    override fun generate(repositoryModel: RepositoryModel, source: SourceModel) {
        val sourceFileSpec = source.toFileSpec()
        val repositoryFileSpec = repositoryModel.toFileSpec(source)

        sourceFileSpec.writeTo(filer)
        repositoryFileSpec.writeTo(filer)
    }

    private fun ParameterModel.toParameterSpec(): ParameterSpec = ParameterSpec(name, type.typeName)

    private fun ParameterModel.toPropertySpec(): PropertySpec = PropertySpec.builder(name, type.typeName)
        .getter(
            FunSpec.getterBuilder()
                .addStatement("return %M()", Constants.MEMBER_NAME_THROW_NO_DATA)
                .build()
        )
        .build()


    private fun FunctionModel.toFunSpec(): FunSpec {
        val builder = FunSpec.builder(name)
            .addParameters(parameters.map { it.toParameterSpec() })
            .addModifiers(modifiers)

        returnType?.let {
            builder.returns(it.typeName)
        }

        return builder.build()
    }

    private fun RepositoryModel.toFileSpec(source: SourceModel): FileSpec {
        val typeSpec = TypeSpec.classBuilder(name)
            .addOriginatingElement(originatingElement)
            .addConstructor(this, source)
            .addFunctions(functions.mapNotNull { it.withImplementation() })
            .build()

        return FileSpec.builder(repositoryPackage, name)
            .addType(typeSpec)
            .build()
    }

    private fun TypeSpec.Builder.addConstructor(repositoryModel: RepositoryModel, source: SourceModel): TypeSpec.Builder {
        val errorHandlerName = Constants.CLASS_NAME_ERROR_HANDLER.simpleName.decapitalize(Locale.US)
        val sourcesName = source.name.decapitalize(Locale.US)

        return superclass(Constants.CLASS_NAME_BASE_OMEGA_REPOSITORY.parameterizedBy(source.className))
            .addModifiers(repositoryModel.generateModifier())
            .addSuperclassConstructorParameter("$errorHandlerName, *$sourcesName")
            .addSuperinterface(repositoryModel.superInterfaceClassName)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(errorHandlerName, Constants.CLASS_NAME_ERROR_HANDLER)
                    .addParameter(sourcesName, source.className, KModifier.VARARG)
                    .build()
            )
    }

    private fun String.decapitalize(locale: Locale): String {
        return if (isNotEmpty() && !this[0].isLowerCase()) substring(0, 1).toLowerCase(locale) + substring(1) else this
    }

    private fun RepositoryModel.generateModifier(): KModifier {
        return functions.firstOrNull {
            !it.modifiers.contains(KModifier.SUSPEND) && it.returnType?.className != Constants.RECEIVE_CHANNEL_CLASS_NAME
        }?.let {
            KModifier.ABSTRACT
        } ?: if (properties.isEmpty()) KModifier.OPEN else KModifier.ABSTRACT
    }

    private fun FunctionModel.withImplementation(): FunSpec? {
        if (!modifiers.contains(KModifier.SUSPEND) && returnType?.className != Constants.RECEIVE_CHANNEL_CLASS_NAME) {
            return null
        }

        return toFunSpec().toBuilder()
            .addModifiers(KModifier.OVERRIDE)
            .addCode(getCodeBody())
            .build()
    }

    private fun FunctionModel.getCodeBody(): CodeBlock {
        val funcWithArguments = name.removeChannelSuffix() + getArguments()
        val strategy = getStrategy()

        return CodeBlock.builder().apply {
            when (returnType?.className) {
                null, Constants.UNIT_CLASS_NAME -> {
                    addStatement("createChannel($strategy) { $funcWithArguments }.%M{}", Constants.MEMBER_NAME_CONSUME_EACH)
                }
                Constants.RECEIVE_CHANNEL_CLASS_NAME -> add("return createChannel($strategy) {  $funcWithArguments } \n")
                else -> add("return createChannel($strategy) { $funcWithArguments }.receive()\n")
            }
        }.build()
    }

    private fun FunctionModel.getStrategy(): String =
        parameters.firstOrNull { it.type.className == Constants.CLASS_NAME_STRATEGY }?.name ?: Constants.REMOTE_ELSE_CACHE

    private fun FunctionModel.getArguments(): String {
        return parameters.filter {
            it.type.className != Constants.CLASS_NAME_STRATEGY
        }.joinToString(prefix = "(", postfix = ")") { it.name }
    }


    private fun SourceModel.toFileSpec(): FileSpec {
        val funcSpecs = functions.map { function ->
            val codeBlock = CodeBlock.builder()

            codeBlock.addStatement(
                when (function.returnType?.className) {
                    null, Constants.UNIT_CLASS_NAME -> "%M()"
                    else -> "return %M()"
                }, Constants.MEMBER_NAME_THROW_NO_DATA
            )

            function.toFunSpec()
                .toBuilder()
                .addCode(codeBlock.build())
                .build()
        }

        val typeSpec = TypeSpec.interfaceBuilder(name)
            .addOriginatingElement(originatingElement)
            .addSuperinterface(Constants.CLASS_NAME_SOURCE)
            .addFunctions(funcSpecs)
            .addProperties(properties.map { it.toPropertySpec() })
            .build()

        return FileSpec.builder(sourcePackage, name)
            .addType(typeSpec)
            .build()
    }

}