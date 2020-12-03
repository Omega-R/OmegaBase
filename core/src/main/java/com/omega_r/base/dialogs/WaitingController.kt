package com.omega_r.base.dialogs

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.omega_r.base.components.OmegaDialog
import com.omega_r.libs.omegatypes.Text

/**
 * Created by Anton Knyazev on 02.12.2020.
 */
private const val KEY_LOCKING_DIALOG_SHOWING = "OMEGA_LOCKING_DIALOG_SHOWING"
open class WaitingController (private val context: Context,
                              private val dialogManager: DialogManager,
                              private val showWaitingDelay: Long = 555L) {

    private val handler = Handler(Looper.getMainLooper())

    private var lockingDialog: LockScreenDialog? = null
        set(value) {
            if (field != value) {
                field?.let {
                    dialogManager.removeDialog(it)
                    it.dismiss()
                }
                value?.let { dialogManager.addDialog(it, DialogCategory.WAITING) }
            }
            field = value
        }
    private var waitingDialog: OmegaDialog? = null
        set(value) {
            if (field != value) {
                field?.let {
                    dialogManager.removeDialog(it)
                    it.dismiss()
                }
                value?.let { dialogManager.addDialog(it, DialogCategory.WAITING) }
            }
            field = value
        }

    private var waitingText: Text? = null


    private val waitingRunnable = Runnable {
        lockingDialog = null

        if (waitingDialog == null) {
            waitingDialog = createWaitingDialog(context)
                .apply(dialogManager::showMessageDialog)
        }
    }

    protected open fun createWaitingDialog(context: Context): OmegaDialog {
        return WaitingDialog(context).apply {
            waitingText?.let {
                text = it
            }
        }
    }

    open fun setWaiting(waiting: Boolean, text: Text?) {
        when (waiting) {
            true -> {
                waitingText = text
                if (lockingDialog == null && waitingDialog == null) {
                    lockingDialog = LockScreenDialog(context).apply {
                        dialogManager.showDialog(this, DialogCategory.WAITING)
                        handler.postDelayed(waitingRunnable, showWaitingDelay)
                    }
                }
                waitingText?.let {
                    (waitingDialog as? TextableOmegaDialog)?.text = it
                }

            }
            false -> {
                handler.removeCallbacks(waitingRunnable)
                waitingText = null
                lockingDialog = null
                waitingDialog = null
            }
        }
    }

    fun saveInstanceState(outState: Bundle) {
        if (lockingDialog != null) {
            outState.putBoolean(KEY_LOCKING_DIALOG_SHOWING, true)
        }
    }

    fun onRestoreInstanceState(savedInstanceState: Bundle) {
        val lockingDialogShowing = savedInstanceState.getBoolean(KEY_LOCKING_DIALOG_SHOWING, false)
        if (lockingDialog != null && !lockingDialogShowing) {
            handler.removeCallbacks(waitingRunnable)
            waitingRunnable.run()
        }
    }

}