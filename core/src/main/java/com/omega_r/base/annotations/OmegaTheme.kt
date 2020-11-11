package com.omega_r.base.annotations

import androidx.annotation.StyleRes

/**
 * Created by Anton Knyazev on 06.04.2019.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Deprecated("This method will be deprecated since Gradle 5.")
annotation class OmegaTheme(@StyleRes val resId: Int)