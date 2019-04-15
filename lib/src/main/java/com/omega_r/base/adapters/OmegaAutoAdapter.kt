package com.omega_r.base.adapters

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.omega_r.base.Identifiable
import com.omega_r.base.adapters.model.AutoBindModel
import org.intellij.lang.annotations.Identifier

/**
 * Created by Anton Knyazev on 07.04.2019.
 */
class OmegaAutoAdapter<M> (
    @LayoutRes private val layoutRes: Int, private val bindModel: AutoBindModel<M>,
    private val callback: Callback<M>? = null
) : OmegaListAdapter<M, OmegaAutoAdapter<M>.ViewHolder>() {

    companion object {

        fun <M> create(@LayoutRes layoutRes: Int,
                       callback: Callback<M>? = null,
                       block: AutoBindModel.Builder<M>.() -> Unit): OmegaAutoAdapter<M> {
            val bindModel = AutoBindModel.create(block)
            return OmegaAutoAdapter(layoutRes, bindModel, callback)
        }

    }

    override fun getItemId(position: Int): Long {
        return when (val item = list[position]) {
            is Identifiable -> item.id
            else -> super.getItemId(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent)

    inner class ViewHolder(viewGroup: ViewGroup) : OmegaListAdapter.ViewHolder<M>(viewGroup, layoutRes) {

        init {
            if (callback != null) {
                setOnClickListener(itemView) {
                    val adapterPosition = adapterPosition
                    callback.onClickItem(list[adapterPosition], adapterPosition)
                }
            }
        }

        override fun bind(item: M) = bindModel.bind(itemView, item)

    }

    interface Callback<M> {
        fun onClickItem(item: M, position: Int)
    }

}