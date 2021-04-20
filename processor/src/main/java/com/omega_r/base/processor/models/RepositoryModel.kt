package com.omega_r.base.processor.models

import com.omega_r.base.processor.Constants.Companion.CLASS_NAME_ERROR_HANDLER
import com.omega_r.base.processor.Constants.Companion.CLASS_NAME_BASE_OMEGA_REPOSITORY
import com.omega_r.base.processor.Constants.Companion.CLASS_NAME_STRATEGY
import com.omega_r.base.processor.Constants.Companion.MEMBER_NAME_CONSUME_EACH
import com.omega_r.base.processor.Constants.Companion.RECEIVE_CHANNEL_CLASS_NAME
import com.omega_r.base.processor.Constants.Companion.REMOTE_ELSE_CACHE
import com.omega_r.base.processor.Constants.Companion.UNIT_CLASS_NAME
import com.omega_r.base.processor.extensions.removeChannelSuffix
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.util.*
import javax.lang.model.element.Element

class RepositoryModel(
    val repositoryPackage: String,
    val name: String,
    val superInterfaceClassName: ClassName,
    val properties: List<ParameterModel>,
    val functions: List<FunctionModel>,
    val originatingElement: Element

) {



}