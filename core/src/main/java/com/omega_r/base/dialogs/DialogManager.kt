package com.omega_r.base.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.omega_r.base.components.OmegaDialog
import com.omega_r.libs.omegatypes.Text

/**
 * Created by Anton Knyazev on 2019-10-15.
 */
open class DialogManager(private val context: Context, private val showWaitingDelay: Long = 555L) {

    private val handler = Handler(Looper.getMainLooper())

    private val dialogList = mutableListOf<Dialog>()

    private var lockingDialog: LockScreenDialog? = null
    private var waitingDialog: OmegaDialog? = null

    private var waitingText: Text? = null

    private val waitingRunnable = Runnable {
        lockingDialog?.dismiss()
        lockingDialog = null

        if (waitingDialog == null) {
            waitingDialog = createWaitingDialog(context).apply {
                show()
            }
        }
    }

    open protected fun createWaitingDialog(context: Context): OmegaDialog {
        return WaitingDialog(context).apply {
            waitingText?.let {
                text = it
            }
        }
    }

    fun setWaiting(waiting: Boolean, text: Text?) {
        when (waiting) {
            true -> {
                waitingText = text
                if (lockingDialog == null && waitingDialog == null) {
                    lockingDialog = LockScreenDialog(context).apply {
                        show()
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
                lockingDialog?.dismiss()
                lockingDialog = null
                waitingDialog?.dismiss()
                waitingDialog = null
            }
        }
    }

    fun addDialog(dialog: Dialog) {
        dialogList += dialog
    }

    fun removeDialog(dialog: Dialog) {
        dialogList -= dialog
    }

    fun hideLastDialog() {
        dialogList.lastOrNull()?.let {
            it.dismiss()
            dialogList.remove(it)
        }
    }

    fun onStart() {
        dialogList.forEach {
            it.show()
        }
        lockingDialog?.show()
        waitingDialog?.show()
    }

    fun onStop() {
        handler.removeCallbacks(waitingRunnable)
        dialogList.forEach {
            it.dismiss()
        }
        waitingDialog?.dismiss()
        lockingDialog?.dismiss()
    }

}