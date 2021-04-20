package com.omega_r.base.processor

import com.google.auto.service.AutoService
import com.omega_r.base.annotations.AppOmegaRepository
import com.omega_r.base.processor.kotlin.KotlinInjection
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.tools.Diagnostic

@AutoService(Process::class)
@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.ISOLATING)
class OmegaRepositoryProcessor : AbstractProcessor() {

    private val messager: Messager
        get() = processingEnv.messager

    private val elements: Elements
        get() = processingEnv.elementUtils

    private lateinit var injection: Injection

    override fun init(environment: ProcessingEnvironment) {
        super.init(environment)
        injection = KotlinInjection(environment)
    }

    override fun getSupportedAnnotationTypes() = setOf(AppOmegaRepository::class.java.canonicalName)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()



    @OptIn(KotlinPoetMetadataPreview::class)
    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (annotations.isEmpty()) {
            return false
        }
        return try {
            processElements(injection, roundEnv)
        } catch (e: Exception) {
            messager.printMessage(
                Diagnostic.Kind.OTHER,
                "OmegaBase compilation failed. Could you copy stack trace above and write us (or make issue on Github)?"
            )
            e.printStackTrace()
            true
        }
    }

    private fun processElements(injection: Injection, roundEnv: RoundEnvironment): Boolean {
        val repositoryParser = injection.createRepositoryParser()
        val sourceParser = injection.createSourceParser()
        val fileGenerator = injection.createFileGenerator()
        roundEnv.getElementsAnnotatedWith(AppOmegaRepository::class.java)
            .mapNotNull(repositoryParser::parse)
            .forEach { repository ->
                val source = sourceParser.parse(repository)
                fileGenerator.generate(repository, source)
            }
        return true
    }

}