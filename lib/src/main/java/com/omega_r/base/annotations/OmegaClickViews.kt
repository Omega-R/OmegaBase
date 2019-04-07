package com.omega_r.base.annotations

import androidx.annotation.IdRes

/**
 * Created by Anton Knyazev on 06.04.2019.
 */

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class OmegaClickViews(@IdRes vararg val ids: Int)