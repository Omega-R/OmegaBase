package com.omegar.mvp_processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
import com.omega_r.base.annotations.AutoPresenterLauncher
import com.omegar.mvp.MvpView
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
import java.lang.RuntimeException

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
        private val ACTIVITY_LAUNCHER_NAME = ClassName("com.omegar.libs.omegalaunchers", "ActivityLauncher")
        private val FRAGMENT_LAUNCHER_NAME = ClassName("com.omegar.libs.omegalaunchers", "FragmentLauncher")
        private val DIALOG_FRAGMENT_LAUNCHER_NAME = ClassName("com.omegar.libs.omegalaunchers", "DialogFragmentLauncher")

        private val ACTIVITY_NAME = ClassName("android.app", "Activity")
        private val FRAGMENT_NAME = ClassName("androidx.fragment.app", "Fragment")
        private val DIALOG_FRAGMENT_NAME = ClassName("androidx.fragment.app", "DialogFragment")

        private val MVP_BASE_PRESENTER_FIELD = ClassName("com.omega_r.base.mvp.factory", "MvpBasePresenterField")

        private val MVP_ACTIVITY_NAME = ClassName("com.omegar.mvp", "MvpAppCompatActivity")
        private val MVP_FRAGMENT_NAME = ClassName("com.omegar.mvp", "MvpAppCompatFragment")
        private val MVP_DIALOG_FRAGMENT_NAME = ClassName("com.omegar.mvp", "MvpAppCompatDialogFragment")

        private val delegateLauncherMap = mapOf(
            DelegateType.ACTIVITY to ACTIVITY_LAUNCHER_NAME,
            DelegateType.FRAGMENT to FRAGMENT_LAUNCHER_NAME,
            DelegateType.DIALOG_FRAGMENT to DIALOG_FRAGMENT_NAME,
        )

        private val mvpDelegateLauncherMap = mapOf(
            DelegateType.ACTIVITY to MVP_ACTIVITY_NAME,
            DelegateType.FRAGMENT to MVP_FRAGMENT_NAME,
            DelegateType.DIALOG_FRAGMENT to MVP_DIALOG_FRAGMENT_NAME,
        )
    }


    override fun process(resolver: Resolver): List<KSAnnotated> {
        val serializableType = resolver.getClassDeclarationByName(SERIALIZABLE.canonicalName)!!.asType(emptyList())
        val parcelableType = resolver.getClassDeclarationByName(PARCELABLE.canonicalName)!!.asType(emptyList())
        val activityType = resolver.getClassDeclarationByName(ACTIVITY_NAME.canonicalName)!!.asType(emptyList())
        val fragmentType = resolver.getClassDeclarationByName(FRAGMENT_NAME.canonicalName)!!.asType(emptyList())
        val dialogFragmentType = resolver.getClassDeclarationByName(DIALOG_FRAGMENT_NAME.canonicalName)!!.asType(emptyList())
        val mvpView = resolver.getClassDeclarationByName(MvpView::class.qualifiedName!!)!!.asStarProjectedType()

        return resolver.getSymbolsWithAnnotation(AutoPresenterLauncher::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
            .filter {
                if (it.validate()) {
                    it.accept(
                        visitor = LauncherGeneratorVisitor(
                            serializableType = serializableType,
                            parcelableType = parcelableType,
                            activityType = activityType,
                            fragmentType = fragmentType,
                            dialogFragmentType = dialogFragmentType,
                            mvpView = mvpView
                        ),
                        data = Unit
                    )
                    false
                } else true
            }
            .toList()
    }

    private inner class LauncherGeneratorVisitor(
        private val serializableType: KSType,
        private val parcelableType: KSType,
        private val activityType: KSType,
        private val fragmentType: KSType,
        private val dialogFragmentType: KSType,
        private val mvpView: KSType,

        ) : KSVisitorVoid() {

        @OptIn(KspExperimental::class)
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            val factoryName = classDeclaration.simpleName.asString() + "Factory"

            val annotation = classDeclaration.getAnnotationsByType(AutoPresenterLauncher::class).first()
            val ksAnnotation = classDeclaration.annotations.first {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == AutoPresenterLauncher::class.qualifiedName
            }
            val targetClass = ksAnnotation.arguments.first { it.name?.asString() == "delegatedClass" }.value as List<KSType>

            val presenterType = if (annotation.localPresenterType) "LOCAL" else "GLOBAL"

            val (_, view) = classDeclaration.getSuperPresenterAndView()
            val viewStateName = view.toClassName().simpleName.replace("View", "MvpViewState")

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
                        .returns(presenterClassName)
                        .addModifiers(KModifier.OVERRIDE)
                        .addParameter("bundle", BUNDLE.copy(true))
                        .addCode(pairParams.generateBundleGetter(presenterClassName))
                        .build()
                        .apply {
                            builder.addFunction(this)
                        }


                    targetClass.forEach { delegatedClass ->
                        val delegateTypeName = delegatedClass.toTypeName()
                        val launcherClassName = delegateLauncherMap[delegatedClass.getDelegateType()]!!
                        FunSpec.builder(
                            if (targetClass.size == 1) "createLauncher" else {
                                "create${delegatedClass.toClassName().simpleName}Launcher"
                            }
                        )
                            .returns(launcherClassName)
                            .addParameters(pairParams.map {
                                val name = it.second.name?.asString()!!
                                ParameterSpec.builder(name, it.second.type.toTypeName())
                                    .build()
                            })
                            .addCode(pairParams.generateBundlePutter(delegateTypeName))
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
                .also { builder ->
                    targetClass.forEach { delegatedClass ->
                        FunSpec.builder("providePresenter")
                            .receiver(delegatedClass.toTypeName())
                            .returns(
                                MVP_BASE_PRESENTER_FIELD.parameterizedBy(
                                    presenterClassName,
                                    mvpDelegateLauncherMap[delegatedClass.getDelegateType()]!!
                                )
                            )
                            .addCode("return with($factoryName) { \n" +
                                    "$viewStateName.Companion\n" +
                                    "createPresenterField() " +
                                    "}")
                            .build()
                            .apply {
                                builder.addFunction(this)
                            }
                    }
                }
                .build()
                .also { fileSpec ->
                    fileSpec.writeTo(codeGenerator = codeGenerator, aggregating = false)
                }
        }

        private fun KSClassDeclaration.getSuperPresenterAndView(): Pair<KSClassDeclaration, KSType> {
            return superTypes
                .firstNotNullOf { reference ->
                    (reference.resolve().declaration as? KSClassDeclaration)
                        ?.takeIf { it.classKind == ClassKind.CLASS }
                        ?.let {
                            it to (reference.findView() ?: throw RuntimeException("It is impossible to find a view in $this"))
                        }
                }
        }

        private fun KSTypeReference.findView(): KSType? {
            val type = element
                ?.typeArguments
                ?.asSequence()
                ?.map { it.type?.resolve() }
                ?.firstOrNull {
                    it?.let {
                        mvpView.isAssignableFrom(it)
                    } ?: false
                }
            return when (type?.declaration) {
                is KSClassDeclaration -> type
                is KSTypeParameter -> {
                    (type.declaration as KSTypeParameter)
                        .bounds
                        .map {
                            it.resolve()
                        }
                        .firstOrNull {
                            mvpView.isAssignableFrom(it)
                        }
                }

                else -> {
                    logger.warn("Unknown type is " + type?.declaration?.let { it::class}.toString())
                    null
                }
            }
        }

        private fun List<Pair<String, KSValueParameter>>.generateBundlePutter(delegatedClass: TypeName): CodeBlock {
            val args = mutableListOf<Any>(delegatedClass)
            val format = "return createLauncher(%T::class, " +
                    joinToString {
                        val ksType = it.second.type.resolve()
                        when (val className = ksType.smartToClassName()) {
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
                when (ksType.smartToClassName()) {
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

        private fun KSType.isSerializable(): Boolean = isImplementation(serializableType)

        private fun KSType.isParcelable(): Boolean = isImplementation(parcelableType)

        private fun KSType.isActivity(): Boolean = activityType.isAssignableFrom(this)

        private fun KSType.isFragment(): Boolean = fragmentType.isAssignableFrom(this)

        private fun KSType.isDialogFragment(): Boolean = dialogFragmentType.isAssignableFrom(this)

        private fun KSType.getDelegateType() = when {
            isActivity() -> DelegateType.ACTIVITY
            isFragment() -> DelegateType.FRAGMENT
            isDialogFragment() -> DelegateType.DIALOG_FRAGMENT
            else -> throw IllegalArgumentException("Unknown type $this")
        }

        private fun KSType.isImplementation(type: KSType): Boolean {
            return when (val declaration = declaration) {
                is KSClassDeclaration -> {
                    declaration.getAllSuperTypes().contains(type)
                }

                is KSTypeAlias -> {
                    return declaration.type.resolve().isSerializable()
                }

                else -> throw IllegalArgumentException()
            }
        }

        private fun KSType.smartToClassName(): ClassName {
            return when (val declaration = declaration) {
                is KSClassDeclaration -> {
                    declaration.toClassName()
                }

                is KSTypeAlias -> {
                    declaration.type.resolve().smartToClassName()
                }

                else -> throw IllegalArgumentException(this::class.simpleName)
            }
        }
    }

    enum class DelegateType {
        ACTIVITY, FRAGMENT, DIALOG_FRAGMENT
    }

}