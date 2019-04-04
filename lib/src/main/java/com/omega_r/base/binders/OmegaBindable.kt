package com.omega_r.base.binders

import android.content.Context
import android.content.res.Resources
import android.view.View
import androidx.annotation.*
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.omega_r.base.OmegaContext
import com.omega_r.base.OmegaViewFindable
import com.omega_r.base.binders.managers.BindersManager
import com.omega_r.libs.omegatypes.Text

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
interface OmegaBindable: OmegaContext, OmegaViewFindable {

    val bindersManager: BindersManager

    private val resources: Resources
        get() = getContext()!!.resources

    fun <T : View> bind(@IdRes res: Int) = bindersManager.bind(BindersManager.BindType.RESETTABLE) {
        findViewById<T>(res)
    }

    fun <T> bind(init: () -> T) = bindersManager.bind(BindersManager.BindType.RESETTABLE, init)

    fun bindColor(@ColorRes res: Int) = bindersManager.bind  {
        ContextCompat.getColor(getContext()!!, res)
    }

    fun bindInt(@IntegerRes res: Int) = bindersManager.bind {
        resources.getInteger(res)
    }

    fun bindDrawable(@DrawableRes res: Int) = bindersManager.bind {
        ContextCompat.getDrawable(getContext()!!, res)!!
    }

    fun bindDimen(@DimenRes res: Int) = bindersManager.bind {
        resources.getDimension(res)
    }

    fun bindDimenPixel(@DimenRes res: Int) = bindersManager.bind {
        resources.getDimensionPixelSize(res)
    }

    fun bindAnimation(@AnimRes res: Int) = bindersManager.bind {
        resources.getAnimation(res)
    }

}