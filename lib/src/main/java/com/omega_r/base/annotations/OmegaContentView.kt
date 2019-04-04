package com.omega_r.base.annotations

import androidx.annotation.LayoutRes

/**
 * Created by Anton Knyazev on 04.04.2019.
 */

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class OmegaContentView(@LayoutRes val layoutRes: Int)