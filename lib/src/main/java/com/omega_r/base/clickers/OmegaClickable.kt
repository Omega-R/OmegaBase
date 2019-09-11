package com.omega_r.base.clickers

import android.view.View
import androidx.core.view.ViewCompat
import com.omega_r.base.OmegaViewFindable

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
interface OmegaClickable: OmegaViewFindable {

    val clickManager: ClickManager

    fun setOnClickListener(itemView: View, block: () -> Unit) {
        if (itemView.id == View.NO_ID) {
            itemView.id  = ViewCompat.generateViewId()
        }
        itemView.setOnClickListener(clickManager.wrap(itemView.id, block))
    }

    fun setOnClickListener(id: Int, listener: View.OnClickListener) {
        findViewById<View>(id)!!.setOnClickListener(clickManager.wrap(id, listener))
    }

    fun setOnClickListenerOptional(id: Int, listener: View.OnClickListener) {
        findViewById<View>(id)?.setOnClickListener(clickManager.wrap(id, listener))
    }

    fun setOnClickListener(id: Int, block: () -> Unit) {
        findViewById<View>(id)!!.setOnClickListener(clickManager.wrap(id, block))
    }

    fun setOnClickListenerOptional(id: Int, block: () -> Unit) {
        findViewById<View>(id)?.setOnClickListener(clickManager.wrap(id, block))
    }

    fun setOnClickListenerWithView(id: Int, block: (View) -> Unit) {
        findViewById<View>(id)!!.setOnClickListener(clickManager.wrap(id, block))
    }

    fun setOnClickListeners(vararg pairs: Pair<Int, () -> Unit>) {
        pairs.forEach { setOnClickListener(it.first, it.second) }
    }

    fun setOnClickListenersOptional(vararg pairs: Pair<Int, () -> Unit>) {
        pairs.forEach { setOnClickListenerOptional(it.first, it.second) }
    }

    fun setOnClickListeners(vararg ids: Int, block: (View) -> Unit) {
        ids.forEach { findViewById<View>(it)!!.setOnClickListener(clickManager.wrap(it, block)) }
    }

    fun setOnClickListenersOptional(vararg ids: Int, block: (View) -> Unit) {
        ids.forEach { findViewById<View>(it)?.setOnClickListener(clickManager.wrap(it, block)) }
    }

    fun <E> setOnClickListeners(vararg pairs: Pair<Int, E>, block: (E) -> Unit) {
        val list = pairs.map { it.first }
        val map = pairs.toMap()
        setOnClickListeners(ids = *list.toIntArray()) {
            block(map[it.id]!!)
        }
    }

    fun <E> setOnClickListenersOptional(vararg pairs: Pair<Int, E>, block: (E) -> Unit) {
        val list = pairs.map { it.first }
        val map = pairs.toMap()
        setOnClickListenersOptional(ids = *list.toIntArray()) {
            block(map[it.id]!!)
        }
    }

    fun setMenuListener(vararg pairs: Pair<Int, () -> Unit>) {
        pairs.forEach { setMenuListener(it.first, it.second) }
    }

    fun setMenuListener(id: Int, block: () -> Unit) {
        clickManager.addMenuClicker(id, block)
    }

}