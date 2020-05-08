package com.omega_r.base.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.omega_r.libs.omegatypes.image.Image
import com.omega_r.libs.omegatypes.image.ImageProcessors
import com.omega_r.libs.omegatypes.image.preload

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
abstract class OmegaListAdapter<M, VH> : OmegaAdapter<VH>(), ListableAdapter<M>
        where VH : RecyclerView.ViewHolder, VH : OmegaListAdapter.ViewHolderBindable<M> {

    override var list: List<M> = emptyList()
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

    class ImagePreloadWatcher<M : Image>(private val adapter: OmegaListAdapter<M, *>) : Watcher {

        private var lastPosition: Int = -1

        override fun bindPosition(position: Int, recyclerView: RecyclerView) {
            val childCount = recyclerView.childCount
            val preloadPosition = if (lastPosition < position) {
                position + childCount
            } else {
                position - childCount
            }


            adapter.list.getOrNull(preloadPosition)?.preload(recyclerView.context)

            lastPosition = position
        }

    }

}