package com.omega_r.base.mvp.model

import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import com.omega_r.libs.omegatypes.Image
import com.omega_r.libs.omegatypes.Text

/**
 * Created by Anton Knyazev on 04.05.2019.
 */
data class Action(
    val name: Text,
    val callback: () -> Unit
)

fun Snackbar.setAction(action: Action?) = apply {
    action?.let {
        setAction(it.name.getCharSequence(context)) {
            action.callback()
        }
    }
}

fun AlertDialog.Builder.setPositiveButton(action: Action?) = apply {
    action?.let {
        setPositiveButton(it.name.getCharSequence(context)) { dialog, which ->
            it.callback()
        }
    }
}

fun AlertDialog.Builder.setNegativeButton(action: Action?) = apply {
    action?.let {
        setNegativeButton(it.name.getCharSequence(context)) { dialog, which ->
            it.callback()
        }
    }
}

fun AlertDialog.Builder.setNeutralButton(action: Action?) = apply {
    action?.let {
        setNeutralButton(it.name.getCharSequence(context)) { dialog, which ->
            it.callback()
        }
    }
}

fun AlertDialog.Builder.setButtons(
    positiveAction: Action?,
    negativeAction: Action?,
    neutralButton: Action?
) = apply {
    setPositiveButton(positiveAction)
    setNegativeButton(negativeAction)
    setNeutralButton(negativeAction)
}