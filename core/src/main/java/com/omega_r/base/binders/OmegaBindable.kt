package com.omega_r.base.binders

import android.animation.AnimatorInflater
import android.content.res.Resources
import android.view.View
import android.view.animation.AnimationUtils
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.AnimatorInflaterCompat
import com.omega_r.base.OmegaContextable
import com.omega_r.base.OmegaViewFindable
import com.omega_r.base.binders.managers.BindersManager
import com.omega_r.base.binders.managers.BindersManager.BindType.RESETTABLE
import com.omega_r.base.binders.managers.BindersManager.BindType.RESETTABLE_WITH_AUTO_INIT

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
interface OmegaBindable : OmegaContextable, OmegaViewFindable {


    private val resources: Resources
        get() = getContext()?.resources ?: error("Context is null")

    val bindersManager: BindersManager

    private fun <T : View> findView(id: Int): T {
        return findViewById(id)
            ?: error("Bind is not found R.id.${this.getContext()!!.resources.getResourceEntryName(id)} (${this::class.java.name})")
    }

    private fun <T : View> findViewOrNull(id: Int): T? {
        return findViewById(id)
    }

    private fun <T : View> findViews(vararg ids: Int): List<T> {
        return ids.map { findView<T>(it) }
    }

    private fun <T : View> findViewsToMap(vararg ids: Int): Map<Int, T> {
        return ids.associate { it to findView<T>(it) }
    }

    private fun <T : View, IH : IdHolder> findViewsToIdHolderMap(ids: Array<out IH>): Map<IH, T> {
        return ids.associate { it to findView<T>(it.id) }
    }

    private fun <T : View, E> findViewsToIdPairMap(ids: Array<out Pair<E, Int>>): Map<E, T> {
        return ids.associate { it.first to findView<T>(it.second) }
    }

    fun <T : RecyclerView> bind(@IdRes res: Int, adapter: RecyclerView.Adapter<*>) =
        bindersManager.bind<T>(RESETTABLE_WITH_AUTO_INIT, { findView(res) }) {
            this.adapter = adapter
        }

    fun <T : RecyclerView> bind(@IdRes res: Int, adapter: RecyclerView.Adapter<*>, initBlock: T.() -> Unit) =
        bindersManager.bind<T>(RESETTABLE_WITH_AUTO_INIT, { findView(res) }) {
            this.adapter = adapter
            initBlock()
        }

    fun <T : View> bind(@IdRes res: Int, initBlock: T.() -> Unit) =
        bindersManager.bind(RESETTABLE_WITH_AUTO_INIT, { findView(res) }, initBlock)

    fun <T : View> bind(@IdRes res: Int) = bindersManager.bind<T>(RESETTABLE_WITH_AUTO_INIT, { findView(res) })

    fun <T> bind(init: () -> T) = bindersManager.bind(RESETTABLE, init)

    fun <T : View> bind(@IdRes vararg ids: Int): Lazy<List<T>> {
        return bindersManager.bind(RESETTABLE, { findViews<T>(*ids) })
    }

    fun <T : View> bind(@IdRes vararg ids: Int, initBlock: T.() -> Unit): Lazy<List<T>> =
        bindersManager.bind(RESETTABLE_WITH_AUTO_INIT, { findViews<T>(*ids) }) {
            forEach(initBlock)
        }

    fun <T : View, IH : IdHolder> bind(ids: Array<out IH>): Lazy<Map<IH, T>> =
        bindersManager.bind(RESETTABLE, { findViewsToIdHolderMap<T, IH>(ids) })

    fun <T : View, IH : IdHolder> bind(ids: Array<out IH>, initBlock: T.(IdHolder) -> Unit): Lazy<Map<IH, T>> =
        bindersManager.bind(RESETTABLE_WITH_AUTO_INIT, { findViewsToIdHolderMap<T, IH>(ids) }) {
            forEach {
                initBlock(it.value, it.key)
            }
        }

    fun <T : View, E> bind(vararg idsPair: Pair<E, Int>) =
        bindersManager.bind(RESETTABLE, { findViewsToIdPairMap<T, E>(idsPair) })

    fun <T : View, E> bind(vararg idsPair: Pair<E, Int>, initBlock: T.(E) -> Unit) =
        bindersManager.bind(RESETTABLE_WITH_AUTO_INIT, { findViewsToIdPairMap<T, E>(idsPair) }) {
            forEach { initBlock(it.value, it.key) }
        }

    fun <T : View> bindOrNull(@IdRes res: Int) = bindersManager.bind(RESETTABLE, { findViewOrNull<T>(res) })

    fun <T : View> bindOrNull(@IdRes res: Int, initBlock: T.() -> Unit) =
        bindersManager.bind(RESETTABLE_WITH_AUTO_INIT, { findViewOrNull<T>(res) }) {
            this?.let(initBlock)
        }

    fun bindColor(@ColorRes res: Int) = bindersManager.bind(findInit = {
        ContextCompat.getColor(getContext()!!, res)
    })

    fun bindInt(@IntegerRes res: Int) = bindersManager.bind(findInit = {
        resources.getInteger(res)
    })

    fun bindDrawable(@DrawableRes res: Int) = bindersManager.bind(findInit = {
        ContextCompat.getDrawable(getContext()!!, res)
            ?: error("BindDrawable is not found R.drawable.${resources.getResourceEntryName(res)} (${this::class.java.name})")
    })

    fun bindDimen(@DimenRes res: Int) = bindersManager.bind(findInit = {
        resources.getDimension(res)
    })

    fun bindDimenPixel(@DimenRes res: Int) = bindersManager.bind(findInit = {
        resources.getDimensionPixelSize(res)
    })

    fun bindAnimation(@AnimRes res: Int) = bindersManager.bind(findInit = {
        AnimationUtils.loadAnimation(getContext(), res)
    })

    fun bindAnimator(@AnimatorRes res: Int) = bindersManager.bind(findInit = {
        AnimatorInflater.loadAnimator(getContext(), res)
    })

}