package com.omega_r.base.components

import com.omega_r.libs.omegatypes.Text
import com.omegar.mvp.MvpView
import com.omegar.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.omegar.mvp.viewstate.strategy.StateStrategyType

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
interface OmegaView : MvpView {

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showMessage(message: Text)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showBottomMessage(message: Text, action: Text? = null, actionListener: (() -> Unit)? = null)

}