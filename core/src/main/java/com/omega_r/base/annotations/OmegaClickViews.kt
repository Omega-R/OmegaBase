package com.omega_r.base.annotations

import androidx.annotation.IdRes

/**
 * Created by Anton Knyazev on 06.04.2019.
 */

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Deprecated("This method will be deprecated since Gradle 5.")
annotation class OmegaClickViews(@IdRes vararg val ids: Int)