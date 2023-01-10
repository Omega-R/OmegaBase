package com.omegar.mvp_processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import com.omega_r.base.annotations.AutoPresenterLauncher
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.KModifier.CONST
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo

class MvpProcessor(
    private val options: Map<String, String>,
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {

    companion object {

        private val PRESENTER_TYPE = ClassName("com.omegar.mvp.presenter", "PresenterType")
        private val MVP_PRESENTER_FACTORY = ClassName("com.omega_r.base.mvp.factory", "MvpPresenterFactory")
        private val BUNDLE = ClassName("android.os", "Bundle")
        private val PARCELABLE = ClassName("android.os", "Parcelable")
        private val SERIALIZABLE = ClassName("java.io", "Serializable")
        private val PUT = MemberName("com.omegar.libs.omegalaunchers.tools", "put", isExtension = true)
        private val ACTIVITY_LAUNCHER = ClassName("com.omega_r.base.mvp.factory", "MvpActivityLauncher")
        private val MVP_ACTIVITY_PRESENTER_FIELD = ClassName("com.omega_r.base.mvp.factory", "MvpActivityPresenterField")
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        return resolver.getSymbolsWithAnnotation(AutoPresenterLauncher::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .filter {
                if (it.validate()) {
                    it.accept(LauncherGeneratorVisitor(resolver), Unit)
                    false
                } else true
            }
            .toList()
    }

    private inner class LauncherGeneratorVisitor(private val resolver: Resolver) : KSVisitorVoid() {

        @OptIn(KspExperimental::class)
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            val factoryName = classDeclaration.simpleName.asString() + "Factory"

            val annotation = classDeclaration.getAnnotationsByType(AutoPresenterLauncher::class).first()
            val ksAnnotation = classDeclaration.annotations.first {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == AutoPresenterLauncher::class.qualifiedName
            }
            val targetClass = ksAnnotation.arguments.first { it.name?.asString() == "delegatedClass" }.value as List<KSType>

            val delegatedClass = targetClass.first().toTypeName()

            val presenterType = if (annotation.localPresenterType) "LOCAL" else "GLOBAL"

            val presenterClassName = classDeclaration.toClassName()

            val typeSpec = TypeSpec.objectBuilder(factoryName)
                .addOriginatingKSFile(classDeclaration.containingFile!!)
                .superclass(MVP_PRESENTER_FACTORY.parameterizedBy(presenterClassName))
                .addSuperclassConstructorParameter("%T.$presenterType, %T::class", PRESENTER_TYPE, presenterClassName)
                .also { builder ->
                    val pairParams: List<Pair<String, KSValueParameter>> =
                        classDeclaration.primaryConstructor!!.parameters.map { parameter ->
                            val paramName = parameter.name!!.asString()
                            val extraName = "KEY_" + paramName.uppercase()
                            builder.addProperty(
                                PropertySpec.builder(extraName, STRING, CONST, PRIVATE)
                                    .initializer("\"$paramName\"")
                                    .build()

                            )
                            extraName to parameter
                        }
                    FunSpec.builder("createPresenter")
                        .addModifiers(KModifier.OVERRIDE)
                        .addParameter("bundle", BUNDLE.copy(true))
                        .addCode(pairParams.generateBundleGetter(presenterClassName))
                        .build()
                        .apply {
                            builder.addFunction(this)
                        }

                    targetClass.forEach { delegatedClass ->
                        FunSpec.builder(if (targetClass.size == 1) "createLauncher" else {
                            "create${delegatedClass.toClassName().simpleName}Launcher"
                        })
                            .addParameters(pairParams.map {
                                val name = it.second.name?.asString()!!
                                ParameterSpec.builder(name, it.second.type.toTypeName())
                                    .build()
                            })
                            .addCode(pairParams.generateBundlePutter(delegatedClass.toTypeName()))
                            .build()
                            .apply {
                                builder.addFunction(this)
                            }
                    }

                    targetClass.forEach { delegatedClass ->
                        FunSpec.builder("providePresenter")
                            .receiver(delegatedClass.toTypeName())
                            .addCode("return createPresenterField()")
                            .build()
                            .apply {
                                builder.addFunction(this)
                            }
                    }

                }
                .build()


            FileSpec.builder(
                packageName = classDeclaration.packageName.asString(),
                fileName = factoryName,
            ).addType(typeSpec)
                .build()
                .also { fileSpec ->
                    fileSpec.writeTo(codeGenerator = codeGenerator, aggregating = true)
                }
        }

        private fun List<Pair<String, KSValueParameter>>.generateBundlePutter(delegatedClass: TypeName): CodeBlock {
            val args = mutableListOf<Any>(delegatedClass)
            val format = "return createLauncher(%T::class, " +
                    joinToString {
                        val ksType = it.second.type.resolve()
                        when (val className = ksType.toClassName()) {
                            LIST, SET -> {
                                val type = ksType.arguments.first().type!!.resolve()
                                val prefix =
                                    if (className == SET) "${ksType.toNullableString()}.toArrayList()" else ""
                                when {
                                    type.isSerializable() -> it.first + " putS " + it.second.name?.asString() + prefix
                                    type.isParcelable() -> it.first + " putP " + it.second.name?.asString() + prefix
                                    else -> throw IllegalArgumentException()
                                }
                            }
                            else -> {
                                args += PUT
                                if (ksType.isSerializable() && ksType.isParcelable()) {
                                    args += PARCELABLE.copy(ksType.isMarkedNullable)
                                    it.first + " %M " + it.second.name?.asString() + " as %T"
                                } else {
                                    it.first + " %M " + it.second.name?.asString()
                                }
                            }
                        }
                    } + ")"
            return CodeBlock.of(format, *args.toTypedArray())
        }

        private fun List<Pair<String, KSValueParameter>>.generateBundleGetter(presenterClassName: ClassName): CodeBlock {
            val builder = CodeBlock.builder()
            builder.add("return %T(", presenterClassName)
            val args = mutableListOf<Any>()
            val format = joinToString {
                val ksType = it.second.type.resolve()
                when (ksType.toClassName()) {
                    SET -> {
                        args += ksType.arguments.first().type?.toTypeName()!!
                        val nullable = ksType.toNullableString()
                        "bundle.get<List<%T>$nullable>(${it.first})$nullable.toSet()"
                    }
                    else -> "bundle get ${it.first}"
                }
            } + ")"
            builder.add(format, *args.toTypedArray())
            return builder.build()
        }

        private fun KSType.toNullableString() = if (isMarkedNullable) "?" else ""

        private fun KSType.isSerializable(): Boolean {
            val serializableType = resolver.getClassDeclarationByName(SERIALIZABLE.canonicalName)!!.asType(emptyList())
            return (declaration as KSClassDeclaration).getAllSuperTypes().contains(serializableType)
        }

        private fun KSType.isParcelable(): Boolean {
            val serializableType = resolver.getClassDeclarationByName(PARCELABLE.canonicalName)!!.asType(emptyList())
            return (declaration as KSClassDeclaration).getAllSuperTypes().contains(serializableType)
        }
    }
}