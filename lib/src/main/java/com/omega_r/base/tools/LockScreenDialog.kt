package com.omega_r.base.tools

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.omega_r.base.R
import com.omega_r.base.components.OmegaDialog
import com.omega_r.libs.omegatypes.Text
import com.omega_r.libs.omegatypes.setText


/**
 * Created by Anton Knyazev on 04.04.2019.
 */
class LockScreenDialog(context: Context) : OmegaDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED)
        super.onCreate(savedInstanceState)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        window?.setDimAmount(0f)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

}