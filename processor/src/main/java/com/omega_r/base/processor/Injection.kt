package com.omega_r.base.processor

import com.omega_r.base.processor.generators.FileGenerator
import com.omega_r.base.processor.parser.RepositoryModelParser
import com.omega_r.base.processor.parser.SourceModelParser

interface Injection {

    fun createRepositoryParser(): RepositoryModelParser

    fun createSourceParser(): SourceModelParser

    fun createFileGenerator(): FileGenerator

}