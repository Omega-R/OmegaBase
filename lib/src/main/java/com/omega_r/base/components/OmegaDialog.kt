package com.omega_r.base.components

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import com.omega_r.base.annotations.OmegaContentView
import com.omega_r.base.binders.OmegaBindable
import com.omega_r.base.binders.managers.BindersManager
import com.omega_r.base.clickers.ClickManager
import com.omega_r.base.clickers.OmegaClickable
import kotlin.reflect.full.findAnnotation

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
open class OmegaDialog: Dialog, OmegaBindable, OmegaClickable {

    override val clickManager = ClickManager()

    override val bindersManager = BindersManager()

    constructor(context: Context) : super(context)

    constructor(context: Context, themeResId: Int) : super(context, themeResId)

    constructor(context: Context,
                cancelable: Boolean,
                cancelListener: DialogInterface.OnCancelListener?) : super(context, cancelable, cancelListener)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val contentView = this::class.findAnnotation<OmegaContentView>()
        if (contentView != null) {
            setContentView(contentView.layoutRes)
        }
    }

}