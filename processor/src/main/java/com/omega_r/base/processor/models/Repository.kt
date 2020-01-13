package com.omega_r.base.processor.models

import com.omega_r.base.processor.Constants.Companion.CLASS_NAME_ERROR_HANDLER
import com.omega_r.base.processor.Constants.Companion.CLASS_NAME_OMEGA_REPOSITORY
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import me.eugeniomarletti.kotlin.metadata.shadow.util.capitalizeDecapitalize.decapitalizeAsciiOnly
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

class Repository(
    val repositoryPackage: String,
    val name: String,
    val functions: List<Function>
) {

    fun toFileSpec(source: Source): FileSpec {
        val typeSpec = TypeSpec.classBuilder(name)
            .addConstructor(source)
            .build()

        return FileSpec.builder(repositoryPackage, name)
            .addType(typeSpec)
            .build()
    }

    private fun TypeSpec.Builder.addConstructor(source: Source): TypeSpec.Builder {
        val errorHandlerName = CLASS_NAME_ERROR_HANDLER.simpleName.decapitalizeAsciiOnly()
        val sourcesName = source.name.decapitalizeAsciiOnly()

        return superclass(CLASS_NAME_OMEGA_REPOSITORY.parameterizedBy(source.className))
            .addModifiers(KModifier.OPEN)
            .addSuperclassConstructorParameter("$errorHandlerName, *$sourcesName")
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(errorHandlerName, CLASS_NAME_ERROR_HANDLER)
                    .addParameter(sourcesName, source.className, KModifier.VARARG)
                    .build()
            )
    }

}