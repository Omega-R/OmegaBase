package com.omega_r.base.components

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.omega_r.base.R
import com.omega_r.base.annotations.OmegaContentView
import com.omega_r.base.annotations.OmegaMenu
import com.omega_r.base.binders.OmegaBindable
import com.omega_r.base.binders.managers.BindersManager
import com.omega_r.base.clickers.ClickManager
import com.omega_r.base.clickers.OmegaClickable
import com.omega_r.libs.omegatypes.Text
import com.omegar.mvp.MvpAppCompatActivity
import kotlin.reflect.full.findAnnotation

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
open class OmegaActivity : MvpAppCompatActivity(), OmegaBindable, OmegaView, OmegaClickable {

    override val clickManager = ClickManager()

    override val bindersManager = BindersManager()

    override fun getContext(): Context? = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val contentView = this::class.findAnnotation<OmegaContentView>()
        contentView?.let { setContentView(it.layoutRes) }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val annotation = this::class.findAnnotation<OmegaMenu>()
        return if (annotation != null) {
            menuInflater.inflate(annotation.menuRes, menu)
            true
        } else {
            super.onCreateOptionsMenu(menu)
        }
    }

    override fun showMessage(message: Text) {
        MaterialAlertDialogBuilder(this)
            .setMessage(message.getString(resources))
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    protected open fun getViewForSnackbar(): View {
        return findViewById(android.R.id.content)!!
    }

    override fun showBottomMessage(message: Text, action: Text?, actionListener: (() -> Unit)?) {
        Snackbar.make(getViewForSnackbar(), message.getString(resources)!!, Snackbar.LENGTH_LONG).apply {
            if (action != null) {
                setAction(action.getString(resources)!!) {
                    actionListener?.invoke()
                }
            }
        }.show()
    }


}