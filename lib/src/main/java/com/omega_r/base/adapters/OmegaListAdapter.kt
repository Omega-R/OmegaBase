package com.omega_r.base.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
abstract class OmegaListAdapter<M, VH> : OmegaAdapter<VH>()
        where VH : RecyclerView.ViewHolder, VH : OmegaListAdapter.ViewHolderBindable<M> {

    var list: List<M> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(list[position])
    }

    abstract class ViewHolder<M> : OmegaAdapter.ViewHolder, ViewHolderBindable<M> {

        constructor(parent: ViewGroup?, res: Int) : super(parent, res)

        constructor(parent: ViewGroup?, layoutInflater: LayoutInflater?, res: Int) : super(parent, layoutInflater, res)

        constructor(itemView: View?) : super(itemView)
    }

    abstract class SwipeViewHolder<M> : OmegaAdapter.SwipeViewHolder, ViewHolderBindable<M> {

        constructor(parent: ViewGroup?, contentRes: Int, swipeLeftMenuRes: Int, swipeRightMenuRes: Int) : super(
            parent,
            contentRes,
            swipeLeftMenuRes,
            swipeRightMenuRes
        )

        constructor(parent: ViewGroup?, contentRes: Int, swipeMenuRes: Int) : super(parent, contentRes, swipeMenuRes)
        constructor(parent: ViewGroup?, contentRes: Int) : super(parent, contentRes)
    }

    interface ViewHolderBindable<M> {

        fun bind(item: M)

    }

}