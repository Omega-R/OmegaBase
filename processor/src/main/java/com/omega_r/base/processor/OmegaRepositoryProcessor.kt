package com.omega_r.base.processor

import com.google.auto.service.AutoService
import com.omega_r.base.annotations.AppOmegaRepository
import com.omega_r.base.processor.factories.RepositoryFactory
import com.omega_r.base.processor.factories.SourceFactory
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Messager
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements

@AutoService(Process::class)
@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.AGGREGATING)
class OmegaRepositoryProcessor : AbstractProcessor() {

    private val messager: Messager
        get() = processingEnv.messager

    private val elements: Elements
        get() = processingEnv.elementUtils

    private lateinit var repositoryFactory: RepositoryFactory
    private val sourceFactory: SourceFactory = SourceFactory()

    override fun init(environment: ProcessingEnvironment?) {
        super.init(environment)
        repositoryFactory = RepositoryFactory(messager, elements)
    }

    override fun getSupportedAnnotationTypes() = setOf(AppOmegaRepository::class.java.canonicalName)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    override fun process(elements: Set<TypeElement>, environment: RoundEnvironment): Boolean {
        repositoryFactory.create(environment.getElementsAnnotatedWith(AppOmegaRepository::class.java))
            .forEach { repository ->
                val source = sourceFactory.create(repository)
                source.toFileSpec()
                    .writeTo(processingEnv.filer)
                repository.toFileSpec(source)
                    .writeTo(processingEnv.filer)
            }
        return true
    }

}