package com.omega_r.base.simple

import android.Manifest
import com.omega_r.base.errors.ErrorHandler
import com.omega_r.base.logs.log
import com.omega_r.base.mvp.model.Action
import com.omega_r.base.mvp.presenters.OmegaPresenter
import com.omega_r.base.simple.dialog_fragment.DialogDialogFragment
import com.omega_r.libs.omegatypes.Text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Retrofit
import java.io.Serializable

/**
 * Created by Anton Knyazev on 06.05.19.
 */
class MainPresenter : OmegaPresenter<MainView>() {

    init {
        viewState.showToast(Text.from(System.getProperty("http.agent")))
        viewState.enabled = false
        launch {
            try {
                Retrofit.Builder()
                    .baseUrl("https://git.omega-r.club")
                    .build()
                    .create(Api::class.java)
                    .test("aga", RequestBody.create(MediaType.get("text/html"), "Run"))
            } catch (e: Exception) {
                throw ErrorHandler().handleThrowable(e)
            }
        }

//       DialogDialogFragment.createLauncher()
//           .launch()
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