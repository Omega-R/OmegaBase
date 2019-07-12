package com.omega_r.base.adapters

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.omega_r.base.adapters.model.AutoBindModel
import com.omega_r.libs.omegarecyclerview.OmegaRecyclerView
import com.omega_r.libs.omegarecyclerview.sticky_decoration.StickyAdapter

/**
 * Created by Anton Knyazev on 2019-07-12.
 */
open class OmegaStickyAutoAdapter<M, VH>(
    viewHolderFactory: Factory<M, VH>,
    private val stickyIdExtractor: (M) -> Long,
    @LayoutRes stickyLayoutRes: Int,
    stickyBindModel: AutoBindModel<M>
) : OmegaAutoAdapter<M, VH>(viewHolderFactory), StickyAdapter<OmegaAutoAdapter.ViewHolder<M>>
        where VH : OmegaRecyclerView.ViewHolder, VH : OmegaListAdapter.ViewHolderBindable<M> {

    companion object {

        fun <M : Any> create(
            @LayoutRes stickyLayoutRes: Int,
            stickyIdExtractor: (M) -> Long,
            stickyBindModel: AutoBindModel<M>
        ) = StickyAdapterBuilder(stickyLayoutRes, stickyIdExtractor, stickyBindModel)

        fun <M : Any> create(
            @LayoutRes stickyLayoutRes: Int,
            stickyIdExtractor: (M) -> Long,
            parentModel: AutoBindModel<M>? = null,
            block: AutoBindModel.Builder<M>.() -> Unit
        ): StickyAdapterBuilder<M> {
            val bindModel = AutoBindModel.create(parentModel, block)
            return create(stickyLayoutRes, stickyIdExtractor, bindModel)
        }

    }

    private val stickyViewHolderFactory = OmegaAutoAdapter.ViewHolderFactory(stickyLayoutRes, stickyBindModel)

    @Suppress("UNCHECKED_CAST")
    override fun onCreateStickyViewHolder(parent: ViewGroup): ViewHolder<M> =
        stickyViewHolderFactory.createViewHolder(parent, this as OmegaAutoAdapter<M, ViewHolder<M>>, 0)


    override fun onBindStickyViewHolder(viewHolder: ViewHolder<M>, position: Int) {
        viewHolder.bind(list[position])
    }

    override fun getStickyId(position: Int) = stickyIdExtractor(list[position])

    class StickyAdapterBuilder<M : Any>(
        @LayoutRes val stickyLayoutRes: Int,
        val stickyIdExtractor: (M) -> Long,
        val stickyBindModel: AutoBindModel<M>
    ) {

        constructor(
            stickyLayoutRes: Int,
            stickyIdExtractor: (M) -> Long,
            parentModel: AutoBindModel<M>? = null,
            block: AutoBindModel.Builder<M>.() -> Unit
        ) : this(stickyLayoutRes, stickyIdExtractor, AutoBindModel.create(parentModel, block))

        fun <VH> create(viewHolderFactory: Factory<M, VH>): OmegaStickyAutoAdapter<M, VH>
                where VH : ViewHolderBindable<M>, VH : OmegaRecyclerView.ViewHolder {
            return OmegaStickyAutoAdapter(viewHolderFactory, stickyIdExtractor, stickyLayoutRes, stickyBindModel)
        }

        fun <VH> createMulti(
            parentModel: AutoBindModel<M>? = null,
            block: MultiAutoAdapterBuilder<M, VH>.() -> Unit
        ): OmegaStickyAutoAdapter<M, VH> where VH : ViewHolderBindable<M>, VH : OmegaRecyclerView.ViewHolder {
            return create(
                MultiAutoAdapterBuilder<M, VH>(parentModel)
                    .also(block)
                    .createFactory()
            )
        }

        fun create(
            @LayoutRes layoutRes: Int,
            callback: ((M) -> Unit)? = null,
            parentModel: AutoBindModel<M>? = null,
            block: AutoBindModel.Builder<M>.() -> Unit
        ): OmegaStickyAutoAdapter<M, ViewHolder<M>> {
            val bindModel = AutoBindModel.create(parentModel, block)
            return create(ViewHolderFactory(layoutRes, bindModel, callback))
        }

        fun create(
            @LayoutRes layoutRes: Int,
            @LayoutRes swipeMenuLayoutRes: Int,
            callback: ((M) -> Unit)? = null,
            parentModel: AutoBindModel<M>? = null,
            block: AutoBindModel.Builder<M>.() -> Unit
        ): OmegaStickyAutoAdapter<M, SwipeViewHolder<M>> {
            val bindModel = AutoBindModel.create(parentModel, block)
            return create(SwipeViewHolderFactory(layoutRes, swipeMenuLayoutRes, bindModel, callback))
        }

        fun create(
            @LayoutRes layoutRes: Int,
            bindModel: AutoBindModel<M>,
            callback: ((M) -> Unit)? = null
        ): OmegaStickyAutoAdapter<M, ViewHolder<M>> {
            return create(ViewHolderFactory(layoutRes, bindModel, callback))
        }

        fun create(
            @LayoutRes layoutRes: Int,
            @LayoutRes swipeMenuLayoutRes: Int,
            bindModel: AutoBindModel<M>,
            callback: ((M) -> Unit)? = null
        ): OmegaStickyAutoAdapter<M, SwipeViewHolder<M>> {
            return create(SwipeViewHolderFactory(layoutRes, swipeMenuLayoutRes, bindModel, callback))
        }


    }


}