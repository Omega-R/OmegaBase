package com.omega_r.base.components

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.omega_r.base.mvp.presenters.OmegaPresenter
import com.omega_r.base.mvp.views.OmegaView
import com.omega_r.base.mvp.model.Action
import com.omega_r.base.mvp.model.setAction
import com.omega_r.base.mvp.model.setButtons
import com.omega_r.base.mvp.model.setPositiveButton
import com.omega_r.bind.delegates.OmegaBindable
import com.omega_r.click.OmegaClickable
import com.omega_r.libs.omegatypes.Text
import com.omegar.libs.omegalaunchers.ActivityLauncher
import com.omegar.libs.omegalaunchers.Launcher
import kotlinx.coroutines.CompletableDeferred

/**
 * Created by Anton Knyazev on 26.04.2019.
 */


internal const val KEY_RESULT = "omegaResultData"

interface OmegaComponent : OmegaBindable, OmegaView, OmegaClickable, OmegaMenuable {

    val presenter: OmegaPresenter<out OmegaView>

    fun <T : View> bindAndSetClick(@IdRes res: Int, block: () -> Unit): Lazy<T> {
        return bind(res) {
            setClickListener(this, block)
        }
    }

    fun createMessage(message: Text, title: Text?, action: Action? = null): Dialog {
        val context = getContext()!!
        return MaterialAlertDialogBuilder(context)
            .setCancelable(true)
            .setTitle(title?.getCharSequence(context))
            .setMessage(message.getCharSequence(context)).apply {
                if (action == null) {
                    setPositiveButton(android.R.string.ok) { _, _ ->
                        presenter.hideQueryOrMessage()
                    }
                } else {
                    setPositiveButton(presenter, action)
                    setOnCancelListener {
                        action()
                        presenter.hideQueryOrMessage()
                    }
                }
            }
            .create()
    }

    fun createQuery(
        message: Text,
        title: Text?,
        positiveAction: Action,
        negativeAction: Action,
        neutralAction: Action?
    ): Dialog {
        val context = getContext()!!

        return MaterialAlertDialogBuilder(context)
            .setTitle(title?.getCharSequence(context))
            .setMessage(message.getCharSequence(context))
            .setCancelable(false)
            .setButtons(presenter, positiveAction, negativeAction, neutralAction)
            .create()
    }

    fun getViewForSnackbar(): View

    override fun showBottomMessage(message: Text, action: Action?) {
        Snackbar.make(
            getViewForSnackbar(),
            message.getCharSequence(getContext()!!)!!,
            Snackbar.LENGTH_LONG
        )
            .setAction(action)
            .show()
    }

    override fun showToast(message: Text) {
        Toast.makeText(getContext(), message.getCharSequence(getContext()!!), Toast.LENGTH_LONG)
            .show()
    }

    override fun launch(launcher: Launcher) {
        launcher.launch(getContext()!!)
    }

    override fun launch(launcher: ActivityLauncher, vararg parentLaunchers: ActivityLauncher) {
        launcher.launch(getContext()!!, *parentLaunchers)
    }

    fun onLaunchResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        return presenter.onLaunchResult(
            requestCode, resultCode == Activity.RESULT_OK,
            data?.getSerializableExtra(KEY_RESULT)
        )
    }

    override fun requestGetPermission(permission: String, deferred: CompletableDeferred<Boolean>) {
        deferred.complete(
            ContextCompat.checkSelfPermission(
                getContext()!!,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

}