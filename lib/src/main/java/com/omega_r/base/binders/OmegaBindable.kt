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
        get() = getContext()?.resources ?: error("Context is null")

    fun <T : View> bind(@IdRes res: Int, initBlock: T.() -> Unit) = bindersManager.bind(RESETTABLE_WITH_AUTO_INIT) {
        val view = findViewById<T>(res) ?: error("Bind is not found R.id.${resources.getResourceEntryName(res)} (${this::class.java.name})")
        initBlock(view)
        view
    }

    fun <T : View> bind(@IdRes res: Int) = bindersManager.bind(RESETTABLE) {
        findViewById<T>(res) ?: error("Bind is not found R.id.${resources.getResourceEntryName(res)} (${this::class.java.name})")
    }

    fun <T> bind(init: () -> T) = bindersManager.bind(RESETTABLE, init)

    fun <T: View> bind(@IdRes vararg ids: Int) = bindersManager.bind(RESETTABLE)  {
        val list = ArrayList<T>(ids.size)
        for (id in ids) {
            list +=  findViewById<T>(id) ?: error("Bind is not found R.id.${resources.getResourceEntryName(id)} (${this::class.java.name})")
        }
        list
    }

    fun <T: View> bind(@IdRes vararg ids: Int, initBlock: T.() -> Unit) = bindersManager.bind(RESETTABLE_WITH_AUTO_INIT) {
        val list = ArrayList<T>(ids.size)
        for (id in ids) {
            val view = findViewById<T>(id) ?: error("Bind is not found R.id.${resources.getResourceEntryName(id)} (${this::class.java.name})")
            list += view
            initBlock(view)
        }
        list
    }

    fun <T: View, IH: IdHolder> bind(ids: Array<out IH>) = bindersManager.bind(RESETTABLE)  {
        val map = HashMap<IH, T>(ids.size)
        for (id in ids) {
            map[id] = findViewById(id.id) ?: error("Bind is not found R.id.${resources.getResourceEntryName(id.id)} (${this::class.java.name})")
        }
        map
    }

    fun <T: View, IH: IdHolder> bind(ids: Array<out IH>, initBlock: T.(IdHolder) -> Unit) = bindersManager.bind(RESETTABLE_WITH_AUTO_INIT)  {
        val map = HashMap<IH, T>(ids.size)
        for (idHolder in ids) {
            val view = findViewById<T>(idHolder.id) ?: error("Bind is not found R.id.${resources.getResourceEntryName(idHolder.id)} (${this::class.java.name})")
            map[idHolder] = view
            initBlock(view, idHolder)
        }

        map
    }

    fun <T: View, E> bind(vararg idsPair: Pair<E, Int>) = bindersManager.bind(RESETTABLE)  {
        val map = HashMap<E, T>(idsPair.size)
        for (idHolder in idsPair) {
            val view = findViewById<T>(idHolder.second) ?: error("Bind is not found R.id.${resources.getResourceEntryName(idHolder.second)} (${this::class.java.name})")
            map[idHolder.first] = view
        }

        map
    }

    fun <T: View, E> bind(vararg idsPair: Pair<E, Int>, initBlock: T.(E) -> Unit) = bindersManager.bind(RESETTABLE_WITH_AUTO_INIT)  {
        val map = HashMap<E, T>(idsPair.size)
        for (idHolder in idsPair) {
            val view = findViewById<T>(idHolder.second) ?: error("Bind is not found R.id.${resources.getResourceEntryName(idHolder.second)} (${this::class.java.name})")
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
        ContextCompat.getDrawable(getContext()!!, res) ?: error("BindDrawable is not found R.drawable.${resources.getResourceEntryName(res)} (${this::class.java.name})")
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