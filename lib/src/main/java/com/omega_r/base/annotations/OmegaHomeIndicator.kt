package com.omega_r.base.annotations

import androidx.annotation.DrawableRes

/**
 * Created by Anton Knyazev on 06.04.2019.
 */

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class OmegaHomeIndicator(@DrawableRes val iconRes: Int = - 1, val isVisible: Boolean = true)