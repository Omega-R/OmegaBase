package com.omega_r.base.simple

import android.Manifest
import com.omega_r.base.enitity.contains
import com.omega_r.base.errors.ErrorHandler
import com.omega_r.base.logs.Logger
import com.omega_r.base.logs.log
import com.omega_r.base.mvp.model.Action
import com.omega_r.base.mvp.presenters.OmegaPresenter
import com.omega_r.libs.omegatypes.Text
import com.omegar.mvp.InjectViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.Serializable
import java.lang.Math.random
import java.lang.NullPointerException

/**
 * Created by Anton Knyazev on 06.05.19.
 */
@InjectViewState
class MainPresenter : OmegaPresenter<MainView>() {

    private val testPresenter= TestRepository(ErrorHandler())
    private var testValue: String by testPresenter.createRepositoryProperty<String, MainPresenter> {
        log {
            "!!! run $it [$type]"
        }
    }

    init {

//        launch {
//            delay(5000)
//            viewState.showToast(Text.from("Go"))
//            viewState.setWaiting(true)
//            delay(5000)
//            viewState.setWaiting(false)
//        }

        launch {
            (1..300).forEach {
                delay((random() * 250).toLong())
                testValue = it.toString()
            }

        }



//        viewState.showMessage(Text.from("test"), Action(Text.from("Test")) {
//            viewState.showToast(Text.from("test"))
//        })
//
//        viewState.showQuery(
//            Text.from("message"),
//            Text.from("title"),
//            positiveAction = Action("Yes"),
//            negativeAction = Action("No")
//        )
    }

    override fun onLaunchResult(requestCode: Int, success: Boolean, data: Serializable?): Boolean {
        viewState.showToast(Text.from("onLaunchResult = $requestCode"))
        return super.onLaunchResult(requestCode, success, data)
    }

}