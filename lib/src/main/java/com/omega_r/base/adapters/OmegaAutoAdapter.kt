package com.omega_r.base.adapters

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.omega_r.base.Identifiable
import com.omega_r.base.adapters.model.AutoBindModel
import com.omega_r.libs.omegarecyclerview.OmegaRecyclerView

/**
 * Created by Anton Knyazev on 07.04.2019.
 */
open class OmegaAutoAdapter<M, VH>(
    @LayoutRes private val layoutRes: Int,
    @LayoutRes private val swipeMenuLayoutRes: Int = NO_ID,
    private val bindModel: AutoBindModel<M>,
    private val viewHolderFactory: Factory<M, VH>,
    private val callback: Callback<M>? = null
) : OmegaListAdapter<M, VH>() where VH : OmegaRecyclerView.ViewHolder, VH : OmegaListAdapter.ViewHolderBindable<M> {

    companion object {

        const val NO_ID = OmegaAdapter.SwipeViewHolder.NO_ID

        fun <M> create(
            @LayoutRes layoutRes: Int,
            callback: Callback<M>? = null,
            block: AutoBindModel.Builder<M>.() -> Unit
        ): OmegaAutoAdapter<M, ViewHolder<M>> {
            val bindModel = AutoBindModel.create(block)
            return OmegaAutoAdapter(layoutRes, NO_ID, bindModel, ViewHolderFactory(), callback)
        }

        fun <M> create(
            @LayoutRes layoutRes: Int,
            @LayoutRes swipeMenuLayoutRes: Int,
            callback: Callback<M>? = null,
            block: AutoBindModel.Builder<M>.() -> Unit
        ): OmegaAutoAdapter<M, SwipeViewHolder<M>> {
            val bindModel = AutoBindModel.create(block)
            return OmegaAutoAdapter(layoutRes, swipeMenuLayoutRes, bindModel, SwipeViewHolderFactory(), callback)
        }

    }


    override fun getItemId(position: Int): Long {
        return when (val item = list[position]) {
            is Identifiable<*> -> item.idAsLong
            else -> super.getItemId(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return viewHolderFactory.createViewHolder(parent, this)
    }

    class ViewHolder<M>(
        viewGroup: ViewGroup,
        private val adapter: OmegaAutoAdapter<M, *>
    ) : OmegaListAdapter.ViewHolder<M>(viewGroup, adapter.layoutRes) {

        init {
            if (adapter.callback != null) {
                setOnClickListener(itemView) {
                    adapter.callback.onClickItem(adapter.list[adapterPosition])
                }
            }
        }

        override fun bind(item: M) = adapter.bindModel.bind(itemView, item)

    }

    class SwipeViewHolder<M>(
        parent: ViewGroup,
        private val adapter: OmegaAutoAdapter<M, *>
    ) : OmegaListAdapter.SwipeViewHolder<M>(
        parent,
        adapter.layoutRes,
        adapter.swipeMenuLayoutRes,
        adapter.swipeMenuLayoutRes
    ) {

        init {
            if (adapter.callback != null) {
                setOnClickListener(itemView) {
                    adapter.callback.onClickItem(adapter.list[adapterPosition])
                }
            }
        }

        override fun bind(item: M) = adapter.bindModel.bind(itemView, item)

    }

    interface Factory<M, VH> where VH : OmegaRecyclerView.ViewHolder, VH : ViewHolderBindable<M> {

        fun createViewHolder(parent: ViewGroup, adapter: OmegaAutoAdapter<M, VH>): VH

    }

    class ViewHolderFactory<M> : Factory<M, ViewHolder<M>> {

        override fun createViewHolder(parent: ViewGroup, adapter: OmegaAutoAdapter<M, ViewHolder<M>>): ViewHolder<M> {
            return ViewHolder(parent, adapter)
        }

    }

    class SwipeViewHolderFactory<M> : Factory<M, SwipeViewHolder<M>> {

        override fun createViewHolder(
            parent: ViewGroup,
            adapter: OmegaAutoAdapter<M, SwipeViewHolder<M>>
        ): SwipeViewHolder<M> {
            return SwipeViewHolder(parent, adapter)
        }

    }

    interface Callback<M> {
        fun onClickItem(item: M)
    }

}