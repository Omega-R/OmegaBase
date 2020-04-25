package com.omega_r.base.annotations

import android.graphics.drawable.ColorDrawable
import android.view.Window
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.omega_r.libs.extensions.context.getColorByAttribute
import com.omega_r.libs.extensions.context.getCompatColor
import com.omega_r.libs.extensions.context.getCompatDrawable

/**
 * Created by Anton Knyazev on 2019-09-23.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class OmegaWindowBackground(
    @DrawableRes val drawableRes: Int = 0,
    @AttrRes val colorAttrRes: Int = 0,
    @ColorRes val colorRes: Int = 0
) {
    fun apply(window: Window) {
        with(window.context) {
            when {
                drawableRes > 0 -> {
                    window.setBackgroundDrawable(getCompatDrawable(drawableRes))
                }
                colorRes > 0 -> {
                    window.setBackgroundDrawable(ColorDrawable(getCompatColor(colorRes)))
                }
                colorAttrRes > 0 -> {
                    window.setBackgroundDrawable(ColorDrawable(getColorByAttribute(colorAttrRes)))
                }
            }
        }
    }
}