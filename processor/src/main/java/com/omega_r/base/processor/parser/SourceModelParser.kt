package com.omega_r.base.processor.parser

import com.omega_r.base.processor.models.RepositoryModel
import com.omega_r.base.processor.models.SourceModel

interface SourceModelParser {

    fun parse(repository: RepositoryModel): SourceModel

}