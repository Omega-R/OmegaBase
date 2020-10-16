package com.omega_r.base.annotations

import androidx.annotation.StyleRes

/**
 * Created by Anton Knyazev on 10.04.19.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Deprecated("This method will be deprecated since Gradle 5.")
annotation class OmegaWindowFlags(val addFlags: Int = 0, val clearFlags: Int = 0)