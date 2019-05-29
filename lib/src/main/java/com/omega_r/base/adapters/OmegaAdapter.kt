package com.omega_r.base.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.omega_r.base.annotations.OmegaClickViews
import com.omega_r.base.clickers.ClickManager
import com.omega_r.base.clickers.OmegaClickable
import com.omega_r.libs.omegarecyclerview.BaseListAdapter
import com.omega_r.libs.omegarecyclerview.OmegaRecyclerView
import com.omega_r.libs.omegarecyclerview.swipe_menu.SwipeViewHolder
import kotlin.reflect.full.findAnnotation

/**
 * Created by Anton Knyazev on 04.04.2019.
 */

private typealias OmegaSwipeViewHolder = SwipeViewHolder

abstract class OmegaAdapter<VH : RecyclerView.ViewHolder>: OmegaRecyclerView.Adapter<VH>() {

    open class ViewHolder: OmegaRecyclerView.ViewHolder, OmegaClickable {

        override val clickManager: ClickManager by lazy { AdapterClickManager(this) }

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

    }

    open class SwipeViewHolder : OmegaSwipeViewHolder, OmegaClickable {

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

        override val clickManager: ClickManager by lazy { AdapterClickManager(this) }

        init {
            this::class.findAnnotation<OmegaClickViews>()?.let {
                setOnClickListeners(ids = *it.ids, block = this::onClickView)
            }
        }

        protected open fun onClickView(view: View) {
            // nothing
        }

    }

    class AdapterClickManager(private val viewHolder: RecyclerView.ViewHolder): ClickManager() {

        override fun canClickHandle(): Boolean {
            return viewHolder.adapterPosition != RecyclerView.NO_POSITION && super.canClickHandle()
        }

    }


}