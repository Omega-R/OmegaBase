package com.omega_r.base.processor

import com.google.auto.service.AutoService
import com.omega_r.base.annotations.GenerateOmegaRepository
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.classKind
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf.Function
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import me.eugeniomarletti.kotlin.metadata.shadow.util.capitalizeDecapitalize.decapitalizeAsciiOnly
import me.eugeniomarletti.kotlin.metadata.shadow.util.capitalizeDecapitalize.decapitalizeSmart
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType
import java.util.*
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Messager
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.tools.Diagnostic.Kind.ERROR

private const val UNIT = "kotlin.Unit"

@AutoService(Process::class)
@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.AGGREGATING)
class OmegaRepositoryProcessor : AbstractProcessor() {

    companion object {
        private val CLASS_NAME_ERROR_HANDLER = ClassName.bestGuess("com.omega_r.base.errors.ErrorHandler")

        private val CLASS_NAME_SOURCE = ClassName.bestGuess("com.omega_r.base.data.sources.Source")
        private val CLASS_NAME_OMEGA_REPOSITORY = ClassName.bestGuess("com.omega_r.base.data.OmegaRepository")
        private val CLASS_NAME_STRATEGY = ClassName.bestGuess("com.omega_r.base.data.OmegaRepository.Strategy")
        private val PARAMETER_SPEC_STRATEGY = ParameterSpec.builder("strategy", CLASS_NAME_STRATEGY)
            .defaultValue("Strategy.CACHE_AND_REMOTE")
            .build()
        private val MEMBER_NAME_CONSUME_EACH = MemberName("kotlinx.coroutines.channels", "consumeEach")

    }

    private val messager: Messager
        get() = processingEnv.messager

    private val elements: Elements
        get() = processingEnv.elementUtils

    override fun getSupportedAnnotationTypes() = setOf(GenerateOmegaRepository::class.java.canonicalName)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun process(elements: Set<TypeElement>, environment: RoundEnvironment): Boolean {
        environment
            .getElementsAnnotatedWith(GenerateOmegaRepository::class.java)
            .forEach(::generateOmegaRepository)
        return true
    }

    private fun generateOmegaRepository(element: Element) {
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
        val fileName = element.getFileName()

        val typeSpec = TypeSpec.classBuilder(fileName)
            .addConstructor(element)
            .addFunctions(typeMetadata)
            .build()

        FileSpec.builder(elementPackage, fileName)
            .addType(typeSpec)
            .build()
            .writeTo(processingEnv.filer)
    }

    private fun Element.getFileName(): String {
        val sourcePrefix = simpleName
            .toString()
            .removeSuffix(CLASS_NAME_SOURCE.simpleName)

        val repositoryClassName = CLASS_NAME_OMEGA_REPOSITORY.simpleName

        return sourcePrefix + repositoryClassName
    }


    private fun TypeSpec.Builder.addConstructor(element: Element): TypeSpec.Builder {
        val errorHandlerName = CLASS_NAME_ERROR_HANDLER.simpleName.decapitalizeAsciiOnly()
        val sourcesName = element.simpleName.toString().decapitalizeAsciiOnly()

        val sourceTypeName = element.asType().asTypeName()

        return superclass(CLASS_NAME_OMEGA_REPOSITORY.parameterizedBy(sourceTypeName))
            .addModifiers(KModifier.OPEN)
            .addSuperclassConstructorParameter("$errorHandlerName, *$sourcesName")
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(errorHandlerName, CLASS_NAME_ERROR_HANDLER)
                    .addParameter(sourcesName, sourceTypeName, KModifier.VARARG)
                    .build()
            )
    }

    private fun TypeSpec.Builder.addFunctions(kotlinMetadata: KotlinClassMetadata) = apply {
        val nameResolver = kotlinMetadata.data.nameResolver
        val classData = kotlinMetadata.data.classProto
        classData.functionList.forEach {
            addFunction(nameResolver, it)
        }
    }

    private fun TypeSpec.Builder.addFunction(resolver: NameResolver, function: Function): TypeSpec.Builder {
        var funcName = resolver.getName(function)
        val parameterSpecs = generateParameters(resolver, function)
        val arguments = parameterSpecs.subList(1, parameterSpecs.size).joinToString { it.name }
        val isUnitFunction = function.isUnitFunction(resolver)

        val codeBlockBuilder = CodeBlock.builder().apply {
            if (isUnitFunction) {
                addStatement("createChannel(strategy) { $funcName($arguments) }.%M{}", MEMBER_NAME_CONSUME_EACH)
            } else {
                add("return createChannel(strategy) { $funcName($arguments) }\n\n")
            }
        }.build()

        val modifiers: MutableList<KModifier> = mutableListOf(KModifier.OPEN)
        if (isUnitFunction) modifiers.add(KModifier.SUSPEND) else funcName += "Channel"

        return addFunction(
            FunSpec.builder(funcName)
                .addModifiers(modifiers)
                .addParameters(parameterSpecs)
                .addCode(codeBlockBuilder)
                .build()
        )
    }

    private fun generateParameters(resolver: NameResolver, function: Function): List<ParameterSpec> {
        val list = mutableListOf(PARAMETER_SPEC_STRATEGY)
        list += function.valueParameterList.map { parameter ->
            val name = resolver.getString(parameter.name)
            val className = ClassName.bestGuess(resolver.getString(parameter.type.className).formatType())
            ParameterSpec(name, className.copy(nullable = parameter.type.nullable))

        }
        return list
    }

    private fun Function.isUnitFunction(resolver: NameResolver) =
        resolver.getString(returnType.className).formatType() == UNIT

    private fun NameResolver.getName(function: Function): String = getString(function.name)

    private fun String.formatType(): String = replace("/", ".")

}