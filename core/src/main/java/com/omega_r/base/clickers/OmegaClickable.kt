package com.omega_r.base.clickers

import android.view.View
import androidx.core.view.ViewCompat
import com.omega_r.base.OmegaViewFindable

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
interface OmegaClickable : OmegaViewFindable {

    val clickManager: ClickManager

    fun <T : View> setClickListener(view: T, block: () -> Unit)  {
        if (view.id == View.NO_ID) {
            view.id = ViewCompat.generateViewId()
        }
        view.setOnClickListener(clickManager.wrap(view.id, block))
    }

    fun setOnClickListener(id: Int, listener: View.OnClickListener) {
        clickManager.setClickListener(id, optional = false, listener = listener)
    }

    fun setOnClickListenerOptional(id: Int, listener: View.OnClickListener) {
        clickManager.setClickListener(id, optional = true, listener = listener)
    }

    fun setClickListener(id: Int, block: () -> Unit) {
        clickManager.setClickListener(id, optional = false, listener = block)
    }

    fun setClickListenerOptional(id: Int, block: () -> Unit) {
        clickManager.setClickListener(id, optional = true, listener = block)
    }

    fun setClickListenerWithView(id: Int, block: (View) -> Unit) {
        clickManager.setClickListener(id, optional = false, listener = block)
    }

    fun setClickListenerWithViewOptional(id: Int, block: (View) -> Unit) {
        clickManager.setClickListener(id, optional = true, listener = block)
    }

    fun setClickListeners(vararg pairs: Pair<Int, () -> Unit>) {
        pairs.forEach { setClickListener(it.first, it.second) }
    }

    fun setClickListenersOptional(vararg pairs: Pair<Int, () -> Unit>) {
        pairs.forEach { setClickListenerOptional(it.first, it.second) }
    }

    fun setClickListeners(vararg ids: Int, block: (View) -> Unit) {
        ids.forEach { setClickListenerWithView(it, block) }
    }

    fun setClickListenersOptional(vararg ids: Int, block: (View) -> Unit) {
        ids.forEach { setClickListenerWithViewOptional(it, block) }
    }

    fun <E> setClickListeners(vararg pairs: Pair<Int, E>, block: (E) -> Unit) {
        val list = pairs.map { it.first }
        val map = pairs.toMap()
        setClickListeners(ids = *list.toIntArray()) {
            block(map[it.id]!!)
        }
    }

    fun <E> setClickListenersOptional(vararg pairs: Pair<Int, E>, block: (E) -> Unit) {
        val list = pairs.map { it.first }
        val map = pairs.toMap()
        setClickListenersOptional(ids = *list.toIntArray()) {
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