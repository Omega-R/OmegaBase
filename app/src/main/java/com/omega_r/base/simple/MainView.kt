package com.omega_r.base.simple

import com.omega_r.base.mvp.views.OmegaView

interface MainView: OmegaView {

    var list: String

    var enabled: Boolean
}

