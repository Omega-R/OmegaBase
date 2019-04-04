package com.omega_r.base.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
abstract class OmegaListAdapter<M, VH>: OmegaAdapter<VH>()
        where VH : OmegaListAdapter.ViewHolder, VH: OmegaListAdapter.ViewHolderBindable {

    private var list : List<M> = listOf()
        set(value) {
        field = value
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(list[position])
    }

    abstract class ViewHolder: OmegaAdapter.ViewHolder, ViewHolderBindable {

        constructor(parent: ViewGroup?, res: Int) : super(parent, res)

        constructor(parent: ViewGroup?, layoutInflater: LayoutInflater?, res: Int) : super(parent, layoutInflater, res)

        constructor(itemView: View?) : super(itemView)

    }

    interface ViewHolderBindable {

        fun <M> bind(item: M)

    }

}