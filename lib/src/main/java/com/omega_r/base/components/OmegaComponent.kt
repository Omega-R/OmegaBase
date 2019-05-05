package com.omega_r.base.components

import android.view.View
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.omega_r.base.binders.OmegaBindable
import com.omega_r.base.clickers.OmegaClickable
import com.omega_r.base.mvp.OmegaView
import com.omega_r.base.mvp.model.*
import com.omega_r.libs.omegatypes.Text

/**
 * Created by Anton Knyazev on 26.04.2019.
 */
interface OmegaComponent : OmegaBindable, OmegaView, OmegaClickable {

    override fun showMessage(message: Text) {
        MaterialAlertDialogBuilder(getContext()!!)
            .setMessage(message.getCharSequence(getContext()!!))
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    override fun showQuery(message: Text, positiveAction: Action, negativeAction: Action, neutralAction: Action?) {
        MaterialAlertDialogBuilder(getContext()!!)
            .setMessage(message.getCharSequence(getContext()!!))
            .setButtons(positiveAction, negativeAction, neutralAction)
            .show()
    }

    override fun hideQuery() {
        // nothing
    }

    fun getViewForSnackbar(): View

    override fun showBottomMessage(message: Text, action: Action?) {
        Snackbar.make(getViewForSnackbar(), message.getCharSequence(getContext()!!)!!, Snackbar.LENGTH_LONG)
            .setAction(action)
            .show()
    }

    override fun showToast(message: Text) {
        Toast.makeText(getContext(), message.getCharSequence(getContext()!!), Toast.LENGTH_LONG).show()
    }

}