package com.omega_r.base.mvp

import com.omega_r.base.mvp.model.Action
import com.omega_r.base.mvp.strategies.RemoveEndTagStrategy
import com.omega_r.libs.omegatypes.Text
import com.omegar.mvp.MvpView
import com.omegar.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.omegar.mvp.viewstate.strategy.AddToEndStrategy
import com.omegar.mvp.viewstate.strategy.OneExecutionStateStrategy
import com.omegar.mvp.viewstate.strategy.StateStrategyType
import kotlin.reflect.KAnnotatedElement

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
interface OmegaView : MvpView {

    companion object {
        private const val TAG_QUERY_OR_MESSAGE= "queryOrMessage"
    }

    @StateStrategyType(AddToEndStrategy::class, tag = TAG_QUERY_OR_MESSAGE)
    fun showMessage(message: Text, action: Action? = null)

    @StateStrategyType(AddToEndStrategy::class, tag = TAG_QUERY_OR_MESSAGE)
    fun showQuery(message: Text, positiveAction: Action, negativeAction: Action, neutralAction: Action? = null)

    @StateStrategyType(RemoveEndTagStrategy::class, tag = TAG_QUERY_OR_MESSAGE)
    fun hideQueryOrMessage()

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showBottomMessage(message: Text, action: Action? = null)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun showToast(message: Text)

    @StateStrategyType(AddToEndSingleStrategy::class)
    fun setWaiting(waiting: Boolean, text: Text? = null)

    @StateStrategyType(OneExecutionStateStrategy::class)
    fun exit()

}

inline fun <reified T : Annotation> KAnnotatedElement.findAnnotation(): T? =
    @Suppress("UNCHECKED_CAST")
    annotations.firstOrNull { it is T } as T?