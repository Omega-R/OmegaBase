package com.omega_r.base.processor.parser

import com.omega_r.base.processor.models.RepositoryModel
import javax.lang.model.element.Element

interface RepositoryModelParser {

    fun parse(element: Element): RepositoryModel?

}