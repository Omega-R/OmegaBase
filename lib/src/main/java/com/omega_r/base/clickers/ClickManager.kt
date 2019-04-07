package com.omega_r.base.clickers

import android.os.SystemClock
import android.view.View
import androidx.annotation.IdRes

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
open class ClickManager(private val minimumInterval: Long = 555L) {

    companion object {
        private var lastClickTimestamp: Long = 0
    }

    private val clickListenerMap = mutableMapOf<Int, View.OnClickListener>()
    private val clickLambdasMap = mutableMapOf<Int,  () -> Unit>()
    private val clickViewLambdasMap = mutableMapOf<Int,  (View) -> Unit>()

    private val clickListenerObject = View.OnClickListener { v ->
        if (canClickHandle()) {
            performClick(v)
        }
    }

    private fun performClick(view: View) {
        val id = view.id
        clickListenerMap[id]?.onClick(view)
            ?: clickLambdasMap[id]?.invoke()
            ?: clickViewLambdasMap[id]?.invoke(view)

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

    fun wrap(@IdRes id: Int, clickListener: View.OnClickListener): View.OnClickListener {
        clickListenerMap[id] = clickListener
        return clickListenerObject
    }

    fun wrap(@IdRes id: Int, listener: () -> Unit): View.OnClickListener {
        clickLambdasMap[id] = listener
        return clickListenerObject
    }

    fun wrap(@IdRes id: Int, listener: (View) -> Unit): View.OnClickListener {
        clickViewLambdasMap[id] = listener
        return clickListenerObject
    }

    fun removeClickListener(@IdRes id: Int) {
        clickListenerMap.remove(id)
    }

}