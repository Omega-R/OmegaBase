package com.omega_r.base.processor

import com.google.auto.service.AutoService
import com.omega_r.base.annotations.OmegaRepository
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.classKind
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf.Function
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.deserialization.NameResolver
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType
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

    private val OMEGA_REPOSITORY_CLASS_NAME = ClassName.bestGuess("com.omega_r.base.data.OmegaRepository")
    private val STRATEGY_CLASS_NAME = ClassName.bestGuess("com.omega_r.base.data.OmegaRepository.Strategy")
    private val STRATEGY_PARAMETER_SPEC = ParameterSpec("strategy", STRATEGY_CLASS_NAME)
    private val CONSUME_EACH_MEMBER_NAME = MemberName("kotlinx.coroutines.channels", "consumeEach")

    private val messager: Messager
        get() = processingEnv.messager

    private val elements: Elements
        get() = processingEnv.elementUtils

    override fun getSupportedAnnotationTypes() = setOf(OmegaRepository::class.java.canonicalName)

    override fun getSupportedSourceVersion() = SourceVersion.latest()

    override fun process(elements: Set<TypeElement>, environment: RoundEnvironment): Boolean {
        val repositoryElements = environment.getElementsAnnotatedWith(OmegaRepository::class.java)
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
            .plus("OmegaRepository")
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
        classData.functionList.forEach {
            addFunction(nameResolver, it)
        }
        return this
    }

    private fun TypeSpec.Builder.addFunction(resolver: NameResolver, function: Function): TypeSpec.Builder {
        var funcName = resolver.getName(function)
        val parameterSpecs = generateParameters(resolver, function)
        val arguments = parameterSpecs.subList(1, parameterSpecs.size).joinToString { it.name }
        val isUnitFunction = function.isUnitFunction(resolver)

        val codeBlockBuilder = CodeBlock.builder().apply {
            if (isUnitFunction) {
                addStatement("createChannel(strategy) { $funcName($arguments) }.%M{}", CONSUME_EACH_MEMBER_NAME)
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
        val list = mutableListOf(STRATEGY_PARAMETER_SPEC)
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