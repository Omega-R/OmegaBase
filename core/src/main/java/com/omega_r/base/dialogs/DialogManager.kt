package com.omega_r.base.dialogs

import android.app.Dialog

/**
 * Created by Anton Knyazev on 2019-10-15.
 */
open class DialogManager {

    private var isRunning = false

    private val dialogs = LinkedHashMap<Dialog, DialogCategory>()

    fun showMessageDialog(dialog: Dialog) = showDialog(dialog, DialogCategory.MESSAGE)

    fun showDialog(dialog: Dialog, category: DialogCategory) {
        addDialog(dialog, category)
        if (isRunning) {
            dialog.show()
        }
    }

    fun addDialog(dialog: Dialog, category: DialogCategory) {
        dialogs[dialog] = category
    }

    fun removeDialog(dialog: Dialog) {
        dialogs -= dialog
    }

    fun dismissLastDialog(vararg categories: DialogCategory) {
        dialogs.entries
            .lastOrNull { it.value in categories }
            ?.let {
                it.key.apply {
                    dismiss()
                    dialogs.remove(this)
                }
            }
    }

    fun onStart() {
        isRunning = true
        dialogs.keys.forEach {
            it.show()
        }
    }

    fun onStop() {
        isRunning = false
        dialogs.keys.forEach {
            it.dismiss()
        }
    }

}