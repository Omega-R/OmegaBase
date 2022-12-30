package com.omega_r.base.annotations

import kotlin.reflect.KClass

@Retention(AnnotationRetention.SOURCE)
@Target(allowedTargets = [AnnotationTarget.CLASS])
annotation class AutoPresenterLauncher(
    vararg val delegatedClass: KClass<*>,
    val localPresenterType: Boolean = true,
)