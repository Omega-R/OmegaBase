package com.omega_r.base

import android.view.View
import androidx.annotation.IdRes

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
interface OmegaViewFindable {

    fun <T : View> findViewById(@IdRes id: Int): T?

}