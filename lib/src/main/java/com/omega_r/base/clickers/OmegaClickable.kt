package com.omega_r.base.clickers

import android.view.View
import com.omega_r.base.OmegaViewFindable

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
interface OmegaClickable: OmegaViewFindable {

    val clickManager: ClickManager

    fun setOnClickListener(id: Int, listener: View.OnClickListener) {
        findViewById<View>(id)!!.setOnClickListener(clickManager.wrap(id, listener))
    }

    fun setOnClickListener(id: Int, block: () -> Unit) {
        findViewById<View>(id)!!.setOnClickListener(clickManager.wrap(id, block))
    }

    fun setOnClickListenerWithView(id: Int, block: (View) -> Unit) {
        findViewById<View>(id)!!.setOnClickListener(clickManager.wrap(id, block))
    }

    fun setOnClickListeners(vararg pairs: Pair<Int, () -> Unit>) {
        for (pair in pairs) {
            setOnClickListener(pair.first, pair.second)
        }
    }

}