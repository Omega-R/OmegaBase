package com.omega_r.base.mvp.strategies

import com.omegar.mvp.MvpView
import com.omegar.mvp.viewstate.ViewCommand
import com.omegar.mvp.viewstate.strategy.StateStrategy

/**
 * Created by Anton Knyazev on 04.05.2019.
 */
class AddToEndSingleTagStrategy : StateStrategy {

    override fun <View : MvpView> beforeApply(
        currentState: MutableList<ViewCommand<View>>,
        incomingCommand: ViewCommand<View>) {

        val iterator = currentState.iterator()

        while (iterator.hasNext()) {
            val entry = iterator.next()

            if (entry.tag == incomingCommand.tag) {
                iterator.remove()
            }
        }

        currentState.add(incomingCommand)

    }

    override fun <View : MvpView?> afterApply(
        currentState: MutableList<ViewCommand<View>>,
        incomingCommand: ViewCommand<View>) {
        //Just do nothing
    }
}