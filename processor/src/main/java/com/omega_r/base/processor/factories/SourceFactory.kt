package com.omega_r.base.processor.factories

import com.omega_r.base.processor.Constants
import com.omega_r.base.processor.Constants.Companion.CLASS_NAME_OMEGA_REPOSITORY
import com.omega_r.base.processor.Constants.Companion.CLASS_NAME_SOURCE
import com.omega_r.base.processor.Constants.Companion.RECEIVE_CHANNEL_CLASS_NAME
import com.omega_r.base.processor.extensions.removeChannelSuffix
import com.omega_r.base.processor.extensions.toLinkedHashSet
import com.omega_r.base.processor.models.*
import com.omega_r.base.processor.models.Function

class SourceFactory {

    fun create(repository: Repository): Source {
        val sourcePackage = repository.repositoryPackage
        val sourceName = repository.name.removeSuffix(CLASS_NAME_OMEGA_REPOSITORY.simpleName)
            .plus(CLASS_NAME_SOURCE.simpleName)
        val functions = repository.functions.filterFunctions()

        return Source(sourcePackage, sourceName, repository.properties, functions)
    }

    private fun List<Function>.filterFunctions(): List<Function> {
        return map { function ->
            Function(
                function.name.removeChannelSuffix(),
                function.parameters.filterArguments(),
                function.modifiers,
                function.returnType.replaceReceiveChannel()
            )
        }
            .toLinkedHashSet() // Remove duplicate functions, after removing channel
            .toList()
    }

    private fun Set<Parameter>.filterArguments(): Set<Parameter> {
        return filter { parameter ->
            parameter.type.className != Constants.CLASS_NAME_STRATEGY
        }.toLinkedHashSet()
    }

    private fun Type?.replaceReceiveChannel(): Type? {
        return when (this?.className) {
            RECEIVE_CHANNEL_CLASS_NAME -> parameterizedBy.firstOrNull()
            else -> this
        }
    }

}