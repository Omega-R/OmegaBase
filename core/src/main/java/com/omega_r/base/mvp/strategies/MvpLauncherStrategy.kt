package com.omega_r.base.mvp.strategies

import com.omega_r.base.mvp.factory.MvpLauncher
import com.omegar.mvp.MvpView
import com.omegar.mvp.viewstate.ViewCommand
import com.omegar.mvp.viewstate.strategy.StateStrategy

object MvpLauncherStrategy : StateStrategy {

    override fun <View : MvpView> beforeApply(currentState: MutableList<ViewCommand<View>>, incomingCommand: ViewCommand<View>) {
        try {
            val declaredField = incomingCommand::class.java.getDeclaredField("launcher")
            declaredField.isAccessible = true
            val launcher = declaredField.get(incomingCommand) as? MvpLauncher
            launcher?.preparePresenter()
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            currentState.add(incomingCommand)
        }
    }

    override fun <View : MvpView> afterApply(currentState: MutableList<ViewCommand<View>>, incomingCommand: ViewCommand<View>) {
        currentState.remove(incomingCommand)
    }
}