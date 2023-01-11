package com.omega_r.base.simple

import android.os.SystemClock
import com.omega_r.base.annotations.AutoPresenterLauncher
import com.omega_r.base.mvp.presenters.OmegaPresenter
import com.omega_r.libs.omegatypes.Text
import java.io.Serializable

/**
 * Created by Anton Knyazev on 06.05.19.
 */
typealias TestEntity2 = TestEntity
@AutoPresenterLauncher(MainActivity::class, TestFragment::class)
class MainPresenter(testEntity: TestEntity?, t2: TestEntity2?): OmegaPresenter<MainView>() {

    companion object {
        var lastTime = SystemClock.elapsedRealtime()
    }

    init {

        val time =  (SystemClock.elapsedRealtime() - lastTime)
        lastTime = SystemClock.elapsedRealtime()

        if (testEntity == null) {
            MainPresenterFactory.createMainActivityLauncher(TestEntity(), TestEntity()).launch()
        } else {
            println("TestAnt: $time")
        }
        viewState.showToast(Text.from(System.getProperty("http.agent")))
        viewState.enabled = false
//        launch {
//            try {
//                Retrofit.Builder()
//                    .baseUrl("https://git.omega-r.club")
//                    .build()
//                    .create(Api::class.java)
//                    .test("aga", RequestBody.create(MediaType.get("text/html"), "Run"))
//            } catch (e: Exception) {
//                throw ErrorHandler().handleThrowable(e)
//            }
//        }

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