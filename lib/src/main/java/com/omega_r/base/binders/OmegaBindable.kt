package com.omega_r.base.binders

import android.content.res.Resources
import android.view.View
import androidx.annotation.*
import androidx.core.content.ContextCompat
import com.omega_r.base.OmegaContext
import com.omega_r.base.OmegaViewFindable
import com.omega_r.base.binders.managers.BindersManager
import com.omega_r.base.binders.managers.BindersManager.BindType.RESETTABLE
import com.omega_r.base.binders.managers.BindersManager.BindType.RESETTABLE_WITH_AUTO_INIT

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
interface OmegaBindable: OmegaContext, OmegaViewFindable {

    val bindersManager: BindersManager

    private val resources: Resources
        get() = getContext()!!.resources

    fun <T : View> bind(@IdRes res: Int, initBlock: T.() -> Unit) = bindersManager.bind(RESETTABLE_WITH_AUTO_INIT) {
        val view = findViewById<T>(res)!!
        initBlock(view)
        view
    }

    fun <T : View> bind(@IdRes res: Int) = bindersManager.bind(RESETTABLE) {
        findViewById<T>(res)!!
    }

    fun <T> bind(init: () -> T) = bindersManager.bind(RESETTABLE, init)

    fun <T: View> bind(@IdRes vararg ids: Int) = bindersManager.bind(RESETTABLE)  {
        val list = ArrayList<T>(ids.size)
        for (id in ids) {
            list +=  findViewById<T>(id)!!
        }
        list
    }

    fun <T: View> bind(@IdRes vararg ids: Int, initBlock: T.() -> Unit) = bindersManager.bind(RESETTABLE_WITH_AUTO_INIT) {
        val list = ArrayList<T>(ids.size)
        for (id in ids) {
            val view = findViewById<T>(id)!!
            list += view
            initBlock(view)
        }
        list
    }

    fun <T: View, IH: IdHolder> bind(ids: Array<out IH>) = bindersManager.bind(RESETTABLE)  {
        val map = HashMap<IH, T>(ids.size)
        for (id in ids) {
            map[id] = findViewById(id.id)!!
        }
        map
    }

    fun <T: View, IH: IdHolder> bind(ids: Array<out IH>, initBlock: T.(IdHolder) -> Unit) = bindersManager.bind(RESETTABLE_WITH_AUTO_INIT)  {
        val map = HashMap<IH, T>(ids.size)
        for (idHolder in ids) {
            val view = findViewById<T>(idHolder.id)!!
            map[idHolder] = view
            initBlock(view, idHolder)
        }

        map
    }

    fun <T: View, IH: IdHolder> bind(vararg idsPair: Pair<IH, Int>) = bindersManager.bind(RESETTABLE)  {
        val map = HashMap<IH, T>(idsPair.size)
        for (idHolder in idsPair) {
            val view = findViewById<T>(idHolder.second)!!
            map[idHolder.first] = view
        }

        map
    }

    fun <T: View, IH: IdHolder> bind(vararg idsPair: Pair<IH, Int>, initBlock: T.(IdHolder) -> Unit) = bindersManager.bind(RESETTABLE_WITH_AUTO_INIT)  {
        val map = HashMap<IH, T>(idsPair.size)
        for (idHolder in idsPair) {
            val view = findViewById<T>(idHolder.second)!!
            map[idHolder.first] = view
            initBlock(view, idHolder.first)
        }

        map
    }

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