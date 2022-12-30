package com.omegar.mvp_processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.outerType
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import com.omega_r.base.annotations.AutoPresenterLauncher
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.CHAR
import com.squareup.kotlinpoet.CHAR_SEQUENCE
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.KModifier.CONST
import com.squareup.kotlinpoet.KModifier.PRIVATE
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import kotlin.reflect.KClass

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
                            val extraName = "EXTRA_" + paramName.uppercase()
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
                        .addCode("return %T(${pairParams.joinToString { "bundle?." + it.generateBundleGetter() }})",
                            presenterClassName)
                        .build()
                        .apply {
                            builder.addFunction(this)
                        }

                    FunSpec.builder("createLauncher")
                        .addParameters(pairParams.map {
                            val name = it.second.name?.asString()!!
                            ParameterSpec.builder(name, it.second.type.toTypeName())
                                .build()
                        })
                        .addCode(
                            "return createLauncher(%T::class, " +
                                    pairParams.joinToString { it.first + " %M " + it.second.name?.asString() } + ")",
                            args = (listOf(delegatedClass) + pairParams.map { PUT }).toTypedArray()
                        )
                        .build()
                        .apply {
                            builder.addFunction(this)
                        }

                    FunSpec.builder("providePresenter")
                        .receiver(delegatedClass)
                        .addCode("return createPresenterField()")
                        .build()
                        .apply {
                            builder.addFunction(this)
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

        private fun Pair<String, KSValueParameter>.generateBundleGetter(): String {
            val ksType = second.type.resolve()
            val nullable = if (!ksType.isMarkedNullable) "!!" else ""
            val castNullable = if (!ksType.isMarkedNullable) "" else "?"
            return when (second.type.toTypeName().copy(false)) {
                STRING -> "getString($first)$nullable"
                INT -> "getInt($first)$nullable"
                BUNDLE -> "getBundle($first)$nullable"
                BOOLEAN -> "getBoolean($first)$nullable"
                BYTE -> "getByte($first)$nullable"
                SHORT -> "getShort($first)$nullable"
                LONG -> "getLong($first)$nullable"
                FLOAT -> "getFloat($first)$nullable"
                DOUBLE -> "getDouble($first)$nullable"
                CHAR -> "getChar($first)$nullable"
                CHAR_SEQUENCE -> "getCharSequence($first)$nullable"
                PARCELABLE -> "getParcelable($first)$nullable"
                SERIALIZABLE -> "getSerializable($first)$nullable"
                else -> {

                    val serializableType = resolver.getClassDeclarationByName(SERIALIZABLE.canonicalName)!!.asType(emptyList())

                    if ((ksType.declaration as KSClassDeclaration).getAllSuperTypes().contains(serializableType)) {
                        return "getSerializable($first) as ${ksType.toClassName().simpleName}$castNullable"
                    }

                    val parcelableType = resolver.getClassDeclarationByName(PARCELABLE.canonicalName)!!.asType(emptyList())

                    if ((ksType.declaration as KSClassDeclaration).getAllSuperTypes().contains(parcelableType)) {
                        return "getParcelable($first) as ${ksType.toClassName().simpleName}$castNullable"
                    }

                    throw IllegalArgumentException()
                }
            }
        }
    }
}