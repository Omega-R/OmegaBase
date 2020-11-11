package com.omega_r.base.annotations

import androidx.annotation.MenuRes

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Deprecated("This method will be deprecated since Gradle 5.")
annotation class OmegaMenu(@MenuRes val menuRes: Int)