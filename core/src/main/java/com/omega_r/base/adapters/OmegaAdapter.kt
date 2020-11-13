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
                setClickListeners(ids = *it.ids, block = this::onClickView)
            }
        }

        protected open fun onClickView(view: View) {
            // nothing
        }

        final override fun <T : View> setClickListener(view: T, block: () -> Unit) {
            super.setClickListener(view, block)
        }

        final override fun setOnClickListener(id: Int, listener: View.OnClickListener) {
            super.setOnClickListener(id, listener)
        }

        final override fun setOnClickListenerOptional(id: Int, listener: View.OnClickListener) {
            super.setOnClickListenerOptional(id, listener)
        }

        final override fun setClickListener(id: Int, block: () -> Unit) {
            super.setClickListener(id, block)
        }

        final override fun setClickListenerOptional(id: Int, block: () -> Unit) {
            super.setClickListenerOptional(id, block)
        }

        final override fun setClickListenerWithView(id: Int, block: (View) -> Unit) {
            super.setClickListenerWithView(id, block)
        }

        final override fun setClickListenerWithViewOptional(id: Int, block: (View) -> Unit) {
            super.setClickListenerWithViewOptional(id, block)
        }

        final override fun setClickListeners(vararg pairs: Pair<Int, () -> Unit>) {
            super.setClickListeners(*pairs)
        }

        final override fun setClickListenersOptional(vararg pairs: Pair<Int, () -> Unit>) {
            super.setClickListenersOptional(*pairs)
        }

        final override fun setClickListeners(vararg ids: Int, block: (View) -> Unit) {
            super.setClickListeners(ids = *ids, block = block)
        }

        final override fun setClickListenersOptional(vararg ids: Int, block: (View) -> Unit) {
            super.setClickListenersOptional(ids = *ids, block = block)
        }

        final override fun <E> setClickListeners(vararg pairs: Pair<Int, E>, block: (E) -> Unit) {
            super.setClickListeners(pairs = *pairs, block = block)
        }

        final override fun <E> setClickListenersOptional(vararg pairs: Pair<Int, E>, block: (E) -> Unit) {
            super.setClickListenersOptional(pairs = *pairs, block = block)
        }

        @JvmName("setClickFunction")
        fun <T : View> T.setClickListener(block: () -> Unit) {
            setClickListener(this, block)
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
                setClickListeners(ids = *it.ids, block = this::onClickView)
            }
        }

        protected open fun onClickView(view: View) {
            // nothing
        }


        final override fun <T : View> setClickListener(view: T, block: () -> Unit) {
            super.setClickListener(view, block)
        }

        final override fun setOnClickListener(id: Int, listener: View.OnClickListener) {
            super.setOnClickListener(id, listener)
        }

        final override fun setOnClickListenerOptional(id: Int, listener: View.OnClickListener) {
            super.setOnClickListenerOptional(id, listener)
        }

        final override fun setClickListener(id: Int, block: () -> Unit) {
            super.setClickListener(id, block)
        }

        final override fun setClickListenerOptional(id: Int, block: () -> Unit) {
            super.setClickListenerOptional(id, block)
        }

        final override fun setClickListenerWithView(id: Int, block: (View) -> Unit) {
            super.setClickListenerWithView(id, block)
        }

        final override fun setClickListenerWithViewOptional(id: Int, block: (View) -> Unit) {
            super.setClickListenerWithViewOptional(id, block)
        }

        final override fun setClickListeners(vararg pairs: Pair<Int, () -> Unit>) {
            super.setClickListeners(*pairs)
        }

        final override fun setClickListenersOptional(vararg pairs: Pair<Int, () -> Unit>) {
            super.setClickListenersOptional(*pairs)
        }

        final override fun setClickListeners(vararg ids: Int, block: (View) -> Unit) {
            super.setClickListeners(ids = *ids, block = block)
        }

        final override fun setClickListenersOptional(vararg ids: Int, block: (View) -> Unit) {
            super.setClickListenersOptional(ids = *ids, block = block)
        }

        final override fun <E> setClickListeners(vararg pairs: Pair<Int, E>, block: (E) -> Unit) {
            super.setClickListeners(pairs = *pairs, block = block)
        }

        final override fun <E> setClickListenersOptional(vararg pairs: Pair<Int, E>, block: (E) -> Unit) {
            super.setClickListenersOptional(pairs = *pairs, block = block)
        }

        @JvmName("setClickFunction")
        fun <T : View> T.setClickListener(block: () -> Unit) {
            setClickListener(this, block)
        }
    }

    class AdapterClickManager(private val viewHolder: RecyclerView.ViewHolder, findable: OmegaViewFindable) :
        ClickManager(findable) {

        override fun canClickHandle(): Boolean {
            return viewHolder.adapterPosition != RecyclerView.NO_POSITION && super.canClickHandle()
        }

    }


}