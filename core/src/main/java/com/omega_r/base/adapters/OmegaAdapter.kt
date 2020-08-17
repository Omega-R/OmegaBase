package com.omega_r.base.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.omega_r.base.OmegaContextable
import com.omega_r.base.OmegaViewFindable
import com.omega_r.base.annotations.OmegaClickViews
import com.omega_r.base.clickers.ClickManager
import com.omega_r.base.clickers.OmegaClickable
import com.omega_r.libs.omegarecyclerview.OmegaRecyclerView
import com.omega_r.libs.omegarecyclerview.swipe_menu.SwipeViewHolder
import kotlin.reflect.full.findAnnotation

/**
 * Created by Anton Knyazev on 04.04.2019.
 */

private typealias OmegaSwipeViewHolder = SwipeViewHolder

abstract class OmegaAdapter<VH : RecyclerView.ViewHolder> : OmegaRecyclerView.Adapter<VH>() {

    protected open var watcher: Watcher? = null


    override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        watcher?.bindPosition(position, recyclerView!!)
    }

    interface Watcher {

        fun bindPosition(position: Int, recyclerView: RecyclerView)

    }

    open class ViewHolder : OmegaRecyclerView.ViewHolder, OmegaClickable, OmegaContextable, OmegaViewFindable {

        override val clickManager: ClickManager by lazy { AdapterClickManager(this, this) }

        constructor(parent: ViewGroup?, res: Int) : super(parent, res)

        constructor(parent: ViewGroup?, layoutInflater: LayoutInflater?, res: Int) : super(parent, layoutInflater, res)

        constructor(itemView: View?) : super(itemView)

        init {
            this::class.findAnnotation<OmegaClickViews>()?.let {
                setOnClickListeners(ids = *it.ids, block = this::onClickView)
            }
        }

        protected open fun onClickView(view: View) {
            // nothing
        }

        final override fun setOnClickListener(itemView: View, block: () -> Unit) {
            super.setOnClickListener(itemView, block)
        }

        final override fun setOnClickListener(id: Int, listener: View.OnClickListener) {
            super.setOnClickListener(id, listener)
        }

        final override fun setOnClickListenerOptional(id: Int, listener: View.OnClickListener) {
            super.setOnClickListenerOptional(id, listener)
        }

        final override fun setOnClickListener(id: Int, block: () -> Unit) {
            super.setOnClickListener(id, block)
        }

        final override fun setOnClickListenerOptional(id: Int, block: () -> Unit) {
            super.setOnClickListenerOptional(id, block)
        }

        final override fun setOnClickListenerWithView(id: Int, block: (View) -> Unit) {
            super.setOnClickListenerWithView(id, block)
        }

        final override fun setOnClickListenerWithViewOptional(id: Int, block: (View) -> Unit) {
            super.setOnClickListenerWithViewOptional(id, block)
        }

        final override fun setOnClickListeners(vararg pairs: Pair<Int, () -> Unit>) {
            super.setOnClickListeners(*pairs)
        }

        final override fun setOnClickListenersOptional(vararg pairs: Pair<Int, () -> Unit>) {
            super.setOnClickListenersOptional(*pairs)
        }

        final override fun setOnClickListeners(vararg ids: Int, block: (View) -> Unit) {
            super.setOnClickListeners(ids = *ids, block = block)
        }

        final override fun setOnClickListenersOptional(vararg ids: Int, block: (View) -> Unit) {
            super.setOnClickListenersOptional(ids = *ids, block = block)
        }

        final override fun <E> setOnClickListeners(vararg pairs: Pair<Int, E>, block: (E) -> Unit) {
            super.setOnClickListeners(pairs = *pairs, block = block)
        }

        final override fun <E> setOnClickListenersOptional(vararg pairs: Pair<Int, E>, block: (E) -> Unit) {
            super.setOnClickListenersOptional(pairs = *pairs, block = block)
        }

    }

    open class SwipeViewHolder : OmegaSwipeViewHolder, OmegaClickable, OmegaContextable, OmegaViewFindable {

        constructor(parent: ViewGroup?, contentRes: Int, swipeLeftMenuRes: Int, swipeRightMenuRes: Int) : super(
            parent,
            contentRes,
            swipeLeftMenuRes,
            swipeRightMenuRes
        )

        constructor(parent: ViewGroup?, contentRes: Int, swipeMenuRes: Int) : super(parent, contentRes, swipeMenuRes)

        constructor(parent: ViewGroup?, contentRes: Int) : super(parent, contentRes)


        companion object {
            const val NO_ID = OmegaSwipeViewHolder.NO_ID
        }

        override val clickManager: ClickManager by lazy { AdapterClickManager(this, this) }

        init {
            this::class.findAnnotation<OmegaClickViews>()?.let {
                setOnClickListeners(ids = *it.ids, block = this::onClickView)
            }
        }

        protected open fun onClickView(view: View) {
            // nothing
        }

        final override fun setOnClickListener(itemView: View, block: () -> Unit) {
            super.setOnClickListener(itemView, block)
        }

        final override fun setOnClickListener(id: Int, listener: View.OnClickListener) {
            super.setOnClickListener(id, listener)
        }

        final override fun setOnClickListenerOptional(id: Int, listener: View.OnClickListener) {
            super.setOnClickListenerOptional(id, listener)
        }

        final override fun setOnClickListener(id: Int, block: () -> Unit) {
            super.setOnClickListener(id, block)
        }

        final override fun setOnClickListenerOptional(id: Int, block: () -> Unit) {
            super.setOnClickListenerOptional(id, block)
        }

        final override fun setOnClickListenerWithView(id: Int, block: (View) -> Unit) {
            super.setOnClickListenerWithView(id, block)
        }

        final override fun setOnClickListenerWithViewOptional(id: Int, block: (View) -> Unit) {
            super.setOnClickListenerWithViewOptional(id, block)
        }

        final override fun setOnClickListeners(vararg pairs: Pair<Int, () -> Unit>) {
            super.setOnClickListeners(*pairs)
        }

        final override fun setOnClickListenersOptional(vararg pairs: Pair<Int, () -> Unit>) {
            super.setOnClickListenersOptional(*pairs)
        }

        final override fun setOnClickListeners(vararg ids: Int, block: (View) -> Unit) {
            super.setOnClickListeners(ids = *ids, block = block)
        }

        final override fun setOnClickListenersOptional(vararg ids: Int, block: (View) -> Unit) {
            super.setOnClickListenersOptional(ids = *ids, block = block)
        }

        final override fun <E> setOnClickListeners(vararg pairs: Pair<Int, E>, block: (E) -> Unit) {
            super.setOnClickListeners(pairs = *pairs, block = block)
        }

        final override fun <E> setOnClickListenersOptional(vararg pairs: Pair<Int, E>, block: (E) -> Unit) {
            super.setOnClickListenersOptional(pairs = *pairs, block = block)
        }
    }

    class AdapterClickManager(private val viewHolder: RecyclerView.ViewHolder, findable: OmegaViewFindable) :
        ClickManager(findable) {

        override fun canClickHandle(): Boolean {
            return viewHolder.adapterPosition != RecyclerView.NO_POSITION && super.canClickHandle()
        }

    }


}