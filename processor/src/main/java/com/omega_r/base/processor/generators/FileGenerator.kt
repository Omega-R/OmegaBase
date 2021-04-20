package com.omega_r.base.processor.generators

import com.omega_r.base.processor.models.RepositoryModel
import com.omega_r.base.processor.models.SourceModel

interface FileGenerator {

    fun generate(repositoryModel: RepositoryModel, source: SourceModel)

}