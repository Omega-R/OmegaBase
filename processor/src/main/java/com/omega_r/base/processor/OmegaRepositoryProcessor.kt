package com.omega_r.base.processor

import com.google.auto.service.AutoService
import com.omega_r.base.annotations.AppOmegaRepository
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.classKind
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf.Function
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Messager
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic.Kind.ERROR

private const val UNIT = "kotlin.Unit"

@AutoService(Process::class)
class OmegaRepositoryProcessor : AbstractProcessor() {

    private val OBJECT_CLASS = ClassName("java.lang", "Object")
    private val OMEGA_REPOSITORY_CLASS_NAME = ClassName.bestGuess("com.omega_r.base.data.OmegaRepository")
    private val STRATEGY_CLASS_NAME = ClassName.bestGuess("com.omega_r.base.data.OmegaRepository.Strategy")
    private val STRATEGY_PARAMETER_SPEC = ParameterSpec("strategy", STRATEGY_CLASS_NAME)

    private val messager: Messager
        get() = processingEnv.messager

    private val elements: Elements
        get() = processingEnv.elementUtils

    private val typeUtils: Types
        get() = processingEnv.typeUtils

    override fun getSupportedAnnotationTypes() = setOf(AppOmegaRepository::class.java.canonicalName)

    override fun getSupportedSourceVersion() = SourceVersion.latest()

    override fun process(elements: Set<TypeElement>, environment: RoundEnvironment): Boolean {
        val repositoryElements = environment.getElementsAnnotatedWith(AppOmegaRepository::class.java)
        repositoryElements.forEach {
            generateRepository(it)
        }
        return true
    }

    private fun generateRepository(element: Element) {
        val typeMetadata = element.kotlinMetadata
        if (element !is TypeElement || typeMetadata !is KotlinClassMetadata) {
            messager.printMessage(ERROR, "@AppOmegaRepository can't be applied to $element: must be a Kotlin interface")
            return
        }

        val proto = typeMetadata.data.classProto
        if (proto.classKind != ProtoBuf.Class.Kind.INTERFACE) {
            messager.printMessage(ERROR, "@AppOmegaRepository can't be applied to $element: must be a Kotlin interface")
            return
        }

        val elementPackage = elements.getPackageOf(element).toString()
        val fileName = element.getFileName(elementPackage)

        val typeSpec = TypeSpec.classBuilder(fileName)
            .addConstructor(element)
            .addFunctions(typeMetadata)
            .build()

        FileSpec.builder(elementPackage, fileName)
            .addType(typeSpec)
            .build()
            .writeTo(processingEnv.filer)
    }

    private fun Element.getFileName(elementPackage: String): String {
        return this.toString()
            .removePrefix("$elementPackage.")
            .removeSuffix("Source")
            .plus("AppOmegaRepository")
    }

    private fun TypeSpec.Builder.addConstructor(element: Element): TypeSpec.Builder {
        val className = ClassName.bestGuess(element.toString())
        return superclass(OMEGA_REPOSITORY_CLASS_NAME.parameterizedBy(className))
            .addModifiers(KModifier.OPEN)
            .addSuperclassConstructorParameter("*sources")
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("sources", ClassName.bestGuess(element.toString()), KModifier.VARARG)
                    .build()
            )
    }

    private fun TypeSpec.Builder.addFunctions(kotlinMetadata: KotlinClassMetadata): TypeSpec.Builder {
        val nameResolver = kotlinMetadata.data.nameResolver
        val classData = kotlinMetadata.data.classProto

        classData.functionList.forEach { function ->
            val returnType = nameResolver.getString(function.returnType.className).formatType()
            when (returnType) {
                UNIT -> addUnitFunction(nameResolver, function)
                else -> addChannelFunction(nameResolver, function)
            }
        }
        return this
    }

    private fun TypeSpec.Builder.addUnitFunction(resolver: NameResolver, function: Function): TypeSpec.Builder {
        val funcName = resolver.getName(function)
        val parameters = generateParameters(resolver, function)
        val arguments = parameters.subList(1, parameters.size)
            .map { it.name }
            .joinToString()

//        ONLY_REMOTE,
//        ONLY_CACHE,
//        REMOTE_ELSE_CACHE,
//        CACHE_ELSE_REMOTE,
//        CACHE_AND_REMOTE,
//        MEMORY_ELSE_CACHE_AND_REMOTE

        val codeBlock = CodeBlock.builder()
            .add("when(strategy) {\n")
            .add("OmegaRepository.Strategy.ONLY_REMOTE -> remoteSource?.$funcName($arguments)\n")
            .add("OmegaRepository.Strategy.ONLY_CACHE -> { \n")
            .add("fileSource?.$funcName($arguments)\n")
            .add("memorySource?.$funcName($arguments)\n")
            .add("} \n")
            .add("else -> { \n")
            .add("// TODO: Future functional \n")
            .add("} \n")
            .add("}")

        return addFunction(
            FunSpec.builder(funcName)
                .addModifiers(KModifier.SUSPEND, KModifier.OPEN, KModifier.PROTECTED)
                .addParameters(parameters)
                .addComment("TODO: Future functional")
                .addCode(codeBlock.build())
                .build()
        )
    }

    private fun TypeSpec.Builder.addChannelFunction(resolver: NameResolver, function: Function): TypeSpec.Builder {
        val funcName = resolver.getName(function)
        val parameters = generateParameters(resolver, function)
        val arguments = parameters.subList(1, parameters.size)
            .map { it.name }
            .joinToString()

        val codeBlock = CodeBlock.builder()
            .add("return createChannel(strategy) { $funcName($arguments) }\n\n")
            .build()

        return addFunction(
            FunSpec.builder(funcName)
                .addParameters(parameters)
                .addCode(codeBlock)
                .build()
        )
    }

    private fun generateParameters(resolver: NameResolver, function: Function): List<ParameterSpec> {
        val list = mutableListOf(STRATEGY_PARAMETER_SPEC)
        list += function.valueParameterList.map { parameter ->
            val name = resolver.getString(parameter.name)
            val className = ClassName.bestGuess(resolver.getString(parameter.type.className).formatType())
            ParameterSpec(name, className.copy(nullable = parameter.type.nullable))

        }
        return list
    }

    private fun NameResolver.getName(function: Function): String = getString(function.name)

    private fun String.formatType(): String = replace("/", ".")

}