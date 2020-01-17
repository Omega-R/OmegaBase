package com.omega_r.base.processor.extensions

import com.omega_r.base.processor.Constants
import javax.lang.model.element.Element
import javax.lang.model.util.Elements

val Element.pureName: String
    get() {
        return simpleName
            .toString()
            .removeSuffix(Constants.CLASS_NAME_SOURCE.simpleName)
            .removeSuffix(Constants.CLASS_NAME_OMEGA_REPOSITORY.simpleName)
            .removeSuffix(Constants.CLASS_NAME_BASE_OMEGA_REPOSITORY.simpleName)
            .removeSuffix("Repository")
    }

val Element.sourceName: String
    get() = pureName + Constants.CLASS_NAME_SOURCE.simpleName

val Element.repositoryName: String
    get() = pureName + Constants.CLASS_NAME_OMEGA_REPOSITORY.simpleName

fun Elements.packageOf(element: Element) = getPackageOf(element).toString()