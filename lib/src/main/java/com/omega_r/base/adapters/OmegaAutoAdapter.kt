package com.omega_r.base.adapters

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.omega_r.base.adapters.model.AutoBindModel

/**
 * Created by Anton Knyazev on 07.04.2019.
 */
class OmegaAutoAdapter<M>(
    @LayoutRes private val layoutRes: Int, private val bindModel: AutoBindModel<M>
) : OmegaListAdapter<M, OmegaAutoAdapter<M>.ViewHolder>() {

    var callback: Callback<M>? = null

    companion object {

        fun <M> create(@LayoutRes layoutRes: Int, block: AutoBindModel.Builder<M>.() -> Unit): OmegaAutoAdapter<M> {
            val bindModel = AutoBindModel.create(block)
            return OmegaAutoAdapter(layoutRes, bindModel)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    inner class ViewHolder(viewGroup: ViewGroup) : OmegaListAdapter.ViewHolder<M>(viewGroup, layoutRes) {

        init {
            setOnClickListener(itemView) {
                val adapterPosition = adapterPosition
                callback?.onClickItem(list[adapterPosition], adapterPosition)
            }
        }

        override fun bind(item: M) = bindModel.bind(itemView, item)

    }

    interface Callback<M> {
        fun onClickItem(item: M, position: Int)
    }

}