package com.omega_r.base.components

import android.os.Bundle
import android.view.View
import com.omega_r.base.mvp.presenters.OmegaPresenter
import com.omega_r.base.mvp.views.OmegaBindView
import com.omega_r.bind.model.BindModel

abstract class OmegaBindFragment<M>: OmegaFragment, OmegaBindView<M> {

    abstract override val presenter: OmegaPresenter<out OmegaBindView<M>>

    protected abstract val bindModel: BindModel<M>

    constructor() : super()

    constructor(contentLayoutId: Int) : super(contentLayoutId)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindModel.onViewCreated(view)
    }

    override fun bind(item: M) {
        bindModel.bind(requireView(), item)
    }

}