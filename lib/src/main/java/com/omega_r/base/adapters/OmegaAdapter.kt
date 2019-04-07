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
import kotlin.reflect.full.findAnnotation

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
abstract class OmegaAdapter<VH : RecyclerView.ViewHolder>: OmegaRecyclerView.Adapter<VH>() {

    open class ViewHolder: OmegaRecyclerView.ViewHolder, OmegaClickable {

        override val clickManager = object:ClickManager() {
            override fun canClickHandle(): Boolean {
                return adapterPosition != RecyclerView.NO_POSITION && super.canClickHandle()
            }
        }

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

}