package com.omega_r.base.components

import com.omega_r.base.mvp.presenters.OmegaPresenter
import com.omega_r.base.mvp.views.OmegaBindView
import com.omega_r.bind.model.BindModel

abstract class OmegaBindActivity<M> : OmegaActivity, OmegaBindView<M> {

    constructor() : super()

    constructor(contentLayoutId: Int) : super(contentLayoutId)

    protected abstract val bindModel: BindModel<M>

    abstract override val presenter: OmegaPresenter<out OmegaBindView<M>>

    override fun onViewCreated() {
        super.onViewCreated()
        bindModel.onViewCreated(this)
    }

    override fun bind(item: M) {
        bindModel.bind(this, item)
    }

}