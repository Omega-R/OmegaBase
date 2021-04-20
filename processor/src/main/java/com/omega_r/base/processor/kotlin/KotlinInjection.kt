package com.omega_r.base.processor.kotlin

import com.omega_r.base.processor.Injection
import com.omega_r.base.processor.generators.FileGenerator
import com.omega_r.base.processor.parser.RepositoryModelParser
import com.omega_r.base.processor.parser.SourceModelParser
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import javax.annotation.processing.ProcessingEnvironment

class KotlinInjection(private val processingEnv: ProcessingEnvironment) : Injection {

    @OptIn(KotlinPoetMetadataPreview::class)
    override fun createRepositoryParser() = KotlinRepositoryModelParser(processingEnv.messager, processingEnv.elementUtils)

    override fun createSourceParser() = KotlinSourceModelFactory()

    override fun createFileGenerator() = KotlinFileGenerator(processingEnv.filer)

}