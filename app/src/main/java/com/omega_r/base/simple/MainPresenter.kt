package com.omega_r.base.simple

import android.Manifest
import com.omega_r.base.enitity.contains
import com.omega_r.base.logs.log
import com.omega_r.base.mvp.model.Action
import com.omega_r.base.mvp.presenters.OmegaPresenter
import com.omega_r.libs.omegatypes.Text
import com.omegar.mvp.InjectViewState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.Serializable

/**
 * Created by Anton Knyazev on 06.05.19.
 */
@InjectViewState
class MainPresenter : OmegaPresenter<MainView>() {

    init {
        launch {
            delay(5000)
            viewState.showToast(Text.from("Go"))
            viewState.setWaiting(true)
            delay(5000)
            viewState.setWaiting(false)
        }

        launch {
            viewState.showToast(Text.from(getPermissionState(Manifest.permission.WRITE_EXTERNAL_STORAGE).toString()))
        }

//        viewState.showMe
//        log {
//            "Message"
//        }ssage(Text.from("test"), Action(Text.from("Test")) {
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