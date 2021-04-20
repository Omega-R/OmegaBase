package com.omega_r.base.simple.dialog_fragment

import android.os.Bundle
import com.omega_r.base.annotations.OmegaContentView
import com.omega_r.base.components.OmegaDialogFragment
import com.omega_r.base.simple.R
import com.omega_r.libs.omegatypes.Text
import com.omegar.libs.omegalaunchers.createDialogFragmentLauncher
import com.omegar.mvp.presenter.InjectPresenter

/**
 * Created by Anton Knyazev on 10.03.2020.
 */
@OmegaContentView(R.layout.activity_main)
class DialogDialogFragment : OmegaDialogFragment(), DialogView {

    companion object {
        fun createLauncher() = createDialogFragmentLauncher()
    }

    @InjectPresenter
    override lateinit var presenter: DialogPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setClickListener(R.id.button) {
            showToast(Text.from("Test from dialog"))
        }
    }

}