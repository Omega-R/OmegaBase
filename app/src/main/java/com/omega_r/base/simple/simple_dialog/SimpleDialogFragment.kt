package com.omega_r.base.simple.simple_dialog

import android.app.Activity
import android.os.Bundle
import com.omega_r.base.components.OmegaDialogFragment
import com.omegar.libs.omegalaunchers.createDialogFragmentLauncher
import com.omegar.mvp.presenter.InjectPresenter

class SimpleDialogFragment : OmegaDialogFragment(), SimpleDialogView {

    companion object {

        fun createLauncher() = createDialogFragmentLauncher()

    }

    @InjectPresenter
    override lateinit var presenter: SimpleDialogPresenter

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

}