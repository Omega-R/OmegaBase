package com.omega_r.base.annotations

import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes

/**
 * Created by Anton Knyazev on 2019-09-23.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class OmegaWindowBackground(
    @DrawableRes val drawableRes: Int = 0,
    @AttrRes val colorAttrRes: Int = 0
)