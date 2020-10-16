package com.omega_r.base.annotations

import androidx.annotation.StringRes

/**
 * Created by Anton Knyazev on 2019-09-19.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Deprecated("This method will be deprecated since Gradle 5.")
annotation class OmegaTitle(@StringRes val resId: Int)