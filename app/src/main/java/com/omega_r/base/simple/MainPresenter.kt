package com.omega_r.base.simple

import com.omega_r.base.mvp.presenters.OmegaPresenter
import com.omega_r.base.mvp.model.Action
import com.omega_r.libs.omegatypes.Text
import com.omegar.mvp.InjectViewState

/**
 * Created by Anton Knyazev on 06.05.19.
 */
@InjectViewState
class MainPresenter : OmegaPresenter<MainView>() {

    init {
        viewState.showMessage(Text.from("test"), Action(Text.from("Test")){
            viewState.showToast(Text.from("test"))
        })

        viewState.showQuery(Text.from("message"), Text.from("title"), positiveAction = Action("Yes") {

        }, negativeAction = Action("No", {

        }))

    }

}