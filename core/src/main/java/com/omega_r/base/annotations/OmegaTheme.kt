package com.omega_r.base.annotations

import androidx.annotation.StyleRes

/**
 * Created by Anton Knyazev on 06.04.2019.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class OmegaTheme(@StyleRes val resId: Int)