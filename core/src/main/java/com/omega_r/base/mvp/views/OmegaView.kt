package com.omega_r.base.mvp.views

import com.omega_r.base.mvp.model.Action
import com.omega_r.base.mvp.strategies.RemoveEndTagStrategy
import com.omega_r.libs.omegatypes.Text
import com.omegar.libs.omegalaunchers.ActivityLauncher
import com.omegar.libs.omegalaunchers.BaseIntentLauncher
import com.omegar.libs.omegalaunchers.DialogFragmentLauncher
import com.omegar.libs.omegalaunchers.Launcher
import com.omegar.mvp.MvpView
import com.omegar.mvp.viewstate.strategy.*
import com.omegar.mvp.viewstate.strategy.StrategyType.*
import kotlinx.coroutines.CompletableDeferred
import java.io.Serializable
import kotlin.reflect.KAnnotatedElement

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
interface OmegaView : MvpView {

    companion object {
        private const val TAG_QUERY_OR_MESSAGE= "queryOrMessage"
    }

    @StateStrategyType(value = ADD_TO_END, tag = TAG_QUERY_OR_MESSAGE)
    fun showMessage(message: Text, action: Action? = null)

    @StateStrategyType(value = ADD_TO_END, tag = TAG_QUERY_OR_MESSAGE)
    fun showQuery(message: Text, title: Text? = null, positiveAction: Action, negativeAction: Action, neutralAction: Action? = null)

    @StateStrategyType(value = CUSTOM, custom = RemoveEndTagStrategy::class, tag = TAG_QUERY_OR_MESSAGE)
    fun hideQueryOrMessage()

    @StateStrategyType(ONE_EXECUTION)
    fun showBottomMessage(message: Text, action: Action? = null)

    @StateStrategyType(ONE_EXECUTION)
    fun showToast(message: Text)

    @StateStrategyType(ADD_TO_END_SINGLE)
    fun setWaiting(waiting: Boolean, text: Text? = null)

    @StateStrategyType(ONE_EXECUTION)
    fun launch(launcher: Launcher)

    @StateStrategyType(ONE_EXECUTION)
    fun launch(launcher: ActivityLauncher, vararg parentLaunchers: ActivityLauncher)

    @StateStrategyType(ONE_EXECUTION)
    fun launch(launcher: DialogFragmentLauncher)

    @StateStrategyType(ONE_EXECUTION)
    fun launchForResult(launcher: BaseIntentLauncher, requestCode: Int)

    @StateStrategyType(ONE_EXECUTION)
    fun launchForResult(launcher: DialogFragmentLauncher, requestCode: Int)

    @StateStrategyType(ONE_EXECUTION)
    fun setResult(success: Boolean, data: Serializable?)

    @StateStrategyType(ONE_EXECUTION)
    fun requestPermissions(requestCode: Int, vararg permissions: String)

    @StateStrategyType(ONE_EXECUTION)
    fun requestGetPermission(permission: String, deferred: CompletableDeferred<Boolean>)

    @StateStrategyType(ONE_EXECUTION)
    fun exit()

}

inline fun <reified T : Annotation> KAnnotatedElement.findAnnotation(): T? =
    @Suppress("UNCHECKED_CAST")
    annotations.firstOrNull { it is T } as T?