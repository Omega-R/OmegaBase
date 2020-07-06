package com.omega_r.base.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import com.omega_r.base.adapters.model.AutoBindModel
import com.omega_r.libs.omegarecyclerview.OmegaRecyclerView
import com.omega_r.libs.omegarecyclerview.pagination.PaginationViewCreator

open class OmegaPaginationAutoAdapter<M, VH>(
    factory: Factory<M, VH>,
    @LayoutRes private val paginationLayoutRes: Int? = null,
    private val paginationBindModel: AutoBindModel<Pagination.Loading>? = null,
    @LayoutRes private val paginationErrorLayoutRes: Int? = null,
    private val paginationErrorBindModel: AutoBindModel<Pagination.Error>? = null
) : OmegaAutoAdapter<M, VH>(factory),
    PaginationViewCreator where VH : OmegaRecyclerView.ViewHolder, VH : OmegaListAdapter.ViewHolderBindable<M> {

    companion object {

        fun create(
            @LayoutRes paginationLayoutRes: Int? = null,
            paginationBindModel: AutoBindModel<Pagination.Loading>? = null,
            @LayoutRes paginationErrorLayoutRes: Int? = null,
            paginationErrorBindModel: AutoBindModel<Pagination.Error>? = null
        ) = PaginationAdapterBuilder(
            paginationLayoutRes,
            paginationBindModel,
            paginationErrorLayoutRes,
            paginationErrorBindModel
        )
    }

    override fun createPaginationView(parent: ViewGroup?, inflater: LayoutInflater?): View? =
        createView(parent, inflater, paginationLayoutRes, paginationBindModel, Pagination.Loading)

    override fun createPaginationErrorView(parent: ViewGroup?, inflater: LayoutInflater?): View? =
        createView(parent, inflater, paginationErrorLayoutRes, paginationErrorBindModel, Pagination.Error)

    private fun <M> createView(
        parent: ViewGroup?,
        inflater: LayoutInflater?,
        @LayoutRes layoutRes: Int?,
        autoBindModel: AutoBindModel<M>?,
        item: M
    ): View? where M : Pagination {
        return layoutRes?.let {
            inflater?.inflate(it, parent, false)
        }?.let { view ->
            autoBindModel?.run {
                onCreateView(view)
                bind(view, item)
            }
            view
        }
    }

    class PaginationAdapterBuilder(
        @LayoutRes private val paginationLayoutRes: Int? = null,
        private val paginationBindModel: AutoBindModel<Pagination.Loading>? = null,
        @LayoutRes private val paginationErrorLayoutRes: Int? = null,
        private val paginationErrorBindModel: AutoBindModel<Pagination.Error>? = null
    ) {

        fun <M> create(
            @LayoutRes layoutRes: Int,
            bindModel: AutoBindModel<M>,
            callback: ((M) -> Unit)? = null
        ): OmegaAutoAdapter<M, ViewHolder<M>> = OmegaPaginationAutoAdapter(
            ViewHolderFactory(layoutRes, bindModel, callback),
            paginationLayoutRes,
            paginationBindModel,
            paginationErrorLayoutRes,
            paginationErrorBindModel
        )

    }

    sealed class Pagination {

        object Loading : Pagination()
        object Error : Pagination()

    }

}