package com.omega_r.base.processor.models

import com.omega_r.base.processor.Constants.Companion.MEMBER_NAME_THROW_NO_DATA
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec

data class ParameterModel(
    val name: String,
    val type: TypeModel
)