package com.omega_r.base.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.omega_r.libs.omegatypes.Image
import kotlin.math.max
import kotlin.math.min

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
abstract class OmegaListAdapter<M, VH> : OmegaAdapter<VH>(), ListableAdapter<M>
        where VH : RecyclerView.ViewHolder, VH : OmegaListAdapter.ViewHolderBindable<M> {

    override var list: List<M> = listOf()
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

    class ImagePreloadWatcher<M : Image>(
        private val adapter: OmegaListAdapter<M, *>,
        private val maxPreloadCount: Int = 4
    ) : Watcher {

        private var lastEnd: Int = 0
        private var lastFirstVisible = -1

        override fun bindPosition(position: Int, recyclerView: RecyclerView) {
            val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
            val context = recyclerView.context
            val firstVisible = layoutManager.findFirstVisibleItemPosition()

            val from: Int
            val to: Int
            if (firstVisible > lastFirstVisible) {
                from = lastEnd
                to = from + maxPreloadCount
                preload(from, to, context)
            } else if (firstVisible < lastFirstVisible) {
                from = firstVisible
                to = from - maxPreloadCount
                preload(from, to, context)
            }
            lastFirstVisible = firstVisible
        }

        private fun preload(from: Int, to: Int, context: Context) {
            val size = adapter.list.size
            val start = max(0, min(from, size))
            val end = max(0, min(to, size))

            if (from < to) {
                // Increasing
                for (i in start until end) {
                    adapter.list.getOrNull(i)?.preload(context)
                }
            } else {
                // Decreasing
                for (i in end - 1 downTo start) {
                    adapter.list.getOrNull(i)?.preload(context)
                }
            }
            lastEnd = end
        }

    }

}