package com.omega_r.base.mvp.strategies

import com.omegar.mvp.MvpView
import com.omegar.mvp.viewstate.ViewCommand
import com.omegar.mvp.viewstate.strategy.StateStrategy

/**
 * Created by Anton Knyazev on 04.05.2019.
 */
class RemoveEndTagStrategy : StateStrategy {

    override fun <View : MvpView> beforeApply(
        currentState: MutableList<ViewCommand<View>>,
        incomingCommand: ViewCommand<View>) {
        val iterator = currentState.listIterator(currentState.size)

        while (iterator.hasPrevious()) {
            val entry = iterator.previous()

            if (entry.tag == incomingCommand.tag) {
                iterator.remove()
                break
            }
        }
    }

    override fun <View : MvpView?> afterApply(
        currentState: MutableList<ViewCommand<View>>,
        incomingCommand: ViewCommand<View>
    ) {
        //Just do nothing
    }


}