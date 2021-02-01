package com.omega_r.base.processor.kotlin

import com.omega_r.base.processor.Constants
import com.omega_r.base.processor.Constants.Companion.CLASS_NAME_OMEGA_REPOSITORY
import com.omega_r.base.processor.Constants.Companion.CLASS_NAME_SOURCE
import com.omega_r.base.processor.Constants.Companion.RECEIVE_CHANNEL_CLASS_NAME
import com.omega_r.base.processor.extensions.removeChannelSuffix
import com.omega_r.base.processor.extensions.toLinkedHashSet
import com.omega_r.base.processor.parser.SourceModelParser
import com.omega_r.base.processor.models.*
import com.omega_r.base.processor.models.FunctionModel

class KotlinSourceModelFactory : SourceModelParser {

    override fun parse(repository: RepositoryModel): SourceModel {
        val sourcePackage = repository.repositoryPackage
        val sourceName = repository.name.removeSuffix(CLASS_NAME_OMEGA_REPOSITORY.simpleName)
            .plus(CLASS_NAME_SOURCE.simpleName)
        val functions = repository.functions.filterFunctions()

        return SourceModel(sourcePackage, sourceName, repository.properties, functions, repository.originatingElement)
    }

    private fun List<FunctionModel>.filterFunctions(): List<FunctionModel> {
        return map { function ->
            FunctionModel(
                function.name.removeChannelSuffix(),
                function.parameters.filterArguments(),
                function.modifiers,
                function.returnType.replaceReceiveChannel()
            )
        }
            .toLinkedHashSet() // Remove duplicate functions, after removing channel
            .toList()
    }

    private fun Set<ParameterModel>.filterArguments(): Set<ParameterModel> {
        return filter { parameter ->
            parameter.type.className != Constants.CLASS_NAME_STRATEGY
        }.toLinkedHashSet()
    }

    private fun TypeModel?.replaceReceiveChannel(): TypeModel? {
        return when (this?.className) {
            RECEIVE_CHANNEL_CLASS_NAME -> parameterizedBy.firstOrNull()
            else -> this
        }
    }

}