package com.omega_r.base.clickers

import android.os.SystemClock
import android.util.SparseArray
import android.util.SparseBooleanArray
import android.view.View
import androidx.annotation.IdRes
import com.omega_r.base.OmegaContextable
import com.omega_r.base.OmegaViewFindable

/**
 * Created by Anton Knyazev on 04.04.2019.
 */

open class ClickManager(clickable: Clickable? = null, private val minimumInterval: Long = 555L) {

    companion object {
        private var lastClickTimestamp: Long = 0
    }

    private val viewClickSparseArray = SparseArray<View.OnClickListener>()
    private val menuClickMap = SparseArray<() -> Unit>()

    var clickable: Clickable? = null
        set(value) {
            field = value?.also {
                for (i in 0 until optionals.size()) {
                    val id = optionals.keyAt(i)
                    val optional = optionals.valueAt(i)
                    it.setOnClickListener(id, clickListenerObject, optional)
                }
            }
        }

    var viewFindable: OmegaViewFindable?
        get() = (clickable as? ViewFindableClickable)?.viewFindable
        set(value) {
            clickable = value?.let { ViewFindableClickable(value) }
        }

    private val optionals = SparseBooleanArray()


    private val clickListenerObject = View.OnClickListener { v ->
        if (canClickHandle()) {
            performClick(v)
        }
    }

    constructor(findable: OmegaViewFindable, minimumInterval: Long = 555L) : this(
        ViewFindableClickable((findable)),
        minimumInterval
    )

    init {
        this.clickable = clickable
    }

    private fun performClick(view: View) {
        viewClickSparseArray[view.id]?.onClick(view)
    }

    protected open fun canClickHandle(): Boolean {
        val uptimeMillis = SystemClock.uptimeMillis()
        val result = (lastClickTimestamp == 0L
                || uptimeMillis - lastClickTimestamp > minimumInterval)
        if (result) {
            lastClickTimestamp = uptimeMillis
        }
        return result
    }

    fun wrap(@IdRes id: Int, viewListener: View.OnClickListener): View.OnClickListener =
        clickListenerObject.also {
            addViewClicker(id, viewListener)
        }

    fun wrap(@IdRes id: Int, lambdaListener: () -> Unit): View.OnClickListener = clickListenerObject.also {
        addViewClicker(id, lambdaListener)
    }

    fun wrap(@IdRes id: Int, viewLambdaListener: (View) -> Unit): View.OnClickListener =
        clickListenerObject.also {
            addViewClicker(id, viewLambdaListener)
        }

    fun addViewClicker(@IdRes id: Int, listener: View.OnClickListener) {
        viewClickSparseArray[id] = listener
    }

    fun addViewClicker(@IdRes id: Int, listener: (View) -> Unit) {
        viewClickSparseArray[id] = listener
    }

    fun addViewClicker(@IdRes id: Int, listener: () -> Unit) {
        viewClickSparseArray[id] = listener
    }

    fun addMenuClicker(@IdRes id: Int, listener: () -> Unit) {
        menuClickMap[id] = listener
    }

    fun handleMenuClick(@IdRes id: Int): Boolean {
        menuClickMap[id]?.invoke() ?: return false
        return true
    }

    fun removeViewClicker(@IdRes id: Int) {
        viewClickSparseArray.remove(id)
    }

    fun setClickListener(id: Int, listener: View.OnClickListener, optional: Boolean) {
        optionals.put(id, optional)
        val wrapListener = wrap(id, listener)
        clickable?.setOnClickListener(id, wrapListener, optional)
    }

    fun setClickListener(id: Int, listener: () -> Unit, optional: Boolean) {
        optionals.put(id, optional)
        val wrapListener = wrap(id, listener)
        clickable?.setOnClickListener(id, wrapListener, optional)
    }

    fun setClickListener(id: Int, listener: (View) -> Unit, optional: Boolean) {
        optionals.put(id, optional)
        val wrapListener = wrap(id, listener)
        clickable?.setOnClickListener(id, wrapListener, optional)
    }

    private operator fun <E> SparseArray<E>.set(id: Int, value: E) = put(id, value)

    private operator fun SparseArray<View.OnClickListener>.set(id: Int, value: () -> Unit) = put(id, LambdaClickListener(value))

    private operator fun SparseArray<View.OnClickListener>.set(id: Int, value: (View) -> Unit) =
        put(id, LambdaWithViewClickListener(value))

    private operator fun SparseBooleanArray.plusAssign(id: Int) = put(id, true)

    private class LambdaClickListener(private val lambda: () -> Unit) : View.OnClickListener {
        override fun onClick(view: View) = lambda()
    }

    private class LambdaWithViewClickListener(private val lambda: (View) -> Unit) : View.OnClickListener {
        override fun onClick(view: View) = lambda(view)
    }

    interface Clickable {
        fun setOnClickListener(@IdRes id: Int, listener: View.OnClickListener, optional: Boolean)
    }

    open class ViewFindableClickable(internal val viewFindable: OmegaViewFindable) : Clickable {

        override fun setOnClickListener(id: Int, listener: View.OnClickListener, optional: Boolean) {
            val resources = (viewFindable as? OmegaContextable)?.getContext()?.resources

            viewFindable.findViewById<View>(id)?.setOnClickListener(listener)
                ?: if (!optional) {
                    if (resources != null) {
                        error("View not found for R.id.${resources.getResourceEntryName(id)}")
                    } else {
                        error("View not found for id = $id")
                    }
                }
        }

    }

}

