package com.omega_r.base.components

import android.view.View
import android.widget.Toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.omega_r.base.binders.OmegaBindable
import com.omega_r.base.clickers.OmegaClickable
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

    fun getViewForSnackbar(): View

    override fun showBottomMessage(message: Text, action: Text?, actionListener: (() -> Unit)?) {
        Snackbar.make(getViewForSnackbar(), message.getCharSequence(getContext()!!)!!, Snackbar.LENGTH_LONG).apply {
            if (action != null) {
                setAction(action.getCharSequence(context)!!) {
                    actionListener?.invoke()
                }
            }
        }.show()
    }

    override fun showToast(message: Text) {
        Toast.makeText(getContext(), message.getString(getContext()!!), Toast.LENGTH_LONG).show()
    }
}