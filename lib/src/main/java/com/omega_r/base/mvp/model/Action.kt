package com.omega_r.base.mvp.model

import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import com.omega_r.base.mvp.OmegaPresenter
import com.omega_r.libs.omegatypes.Image
import com.omega_r.libs.omegatypes.Text

/**
 * Created by Anton Knyazev on 04.05.2019.
 */
data class Action(val name: Text, val callback: () -> Unit) {

    constructor(action: Action, callback: () -> Unit) : this(action.name, callback)

    constructor(@StringRes nameRes: Int, callback: () -> Unit): this(Text.from(nameRes), callback)

    constructor(name: String, callback: () -> Unit): this(Text.from(name), callback)

    constructor(@StringRes nameRes: Int): this(Text.from(nameRes), {})


    operator fun invoke() {
        callback()
    }

}

fun Snackbar.setAction(action: Action?) = apply {
    action?.let {
        setAction(it.name.getCharSequence(context)) {
            action()
        }
    }
}

fun AlertDialog.Builder.setPositiveButton(presenter: OmegaPresenter<*>, action: Action?) = apply {
    action?.let {
        setPositiveButton(it.name.getCharSequence(context)) { _, _ ->
            it()
            presenter.hideQueryOrMessage()
        }
    }
}

fun AlertDialog.Builder.setNegativeButton(presenter: OmegaPresenter<*>, action: Action?) = apply {
    action?.let {
        setNegativeButton(it.name.getCharSequence(context)) { _, _ ->
            it()
            presenter.hideQueryOrMessage()
        }
    }
}

fun AlertDialog.Builder.setNeutralButton(presenter: OmegaPresenter<*>, action: Action?) = apply {
    action?.let {
        setNeutralButton(it.name.getCharSequence(context)) { _, _ ->
            it()
            presenter.hideQueryOrMessage()
        }
    }
}

fun AlertDialog.Builder.setButtons(
    presenter: OmegaPresenter<*>,
    positiveAction: Action?,
    negativeAction: Action?,
    neutralButton: Action?
) = apply {
    setPositiveButton(presenter, positiveAction)
    setNegativeButton(presenter, negativeAction)
    setNeutralButton(presenter, neutralButton)
}