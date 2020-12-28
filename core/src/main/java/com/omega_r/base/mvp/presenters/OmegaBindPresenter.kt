package com.omega_r.base.mvp.presenters

import com.omega_r.base.mvp.views.OmegaBindView

class OmegaBindPresenter<VIEW: OmegaBindView<M>, M> : OmegaPresenter<VIEW>()