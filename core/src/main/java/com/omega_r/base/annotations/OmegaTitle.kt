package com.omega_r.base.annotations

import androidx.annotation.StringRes

/**
 * Created by Anton Knyazev on 2019-09-19.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class OmegaTitle(@StringRes val resId: Int)