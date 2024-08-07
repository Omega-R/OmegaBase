package com.omega_r.base.mvp.views

import com.omegar.mvp.viewstate.strategy.MoxyViewCommand
import com.omegar.mvp.viewstate.strategy.StateStrategyType
import com.omegar.mvp.viewstate.strategy.StrategyType

interface OmegaBindView<M>: OmegaView {

    @MoxyViewCommand(StrategyType.ADD_TO_END_SINGLE)
    fun bind(item: M)

}