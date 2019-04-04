package com.omega_r.base.tools

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import com.omega_r.base.R
import com.omega_r.base.components.OmegaDialog
import com.omega_r.libs.omegatypes.Text
import com.omega_r.libs.omegatypes.setText

/**
 * Created by Anton Knyazev on 04.04.2019.
 */
class WaitingDialog(context: Context) : OmegaDialog(context) {

    private val handler = Handler(Looper.getMainLooper())

    private var view: View? = null

    var text: Text = Text.from(R.string.loading)
        set(value) {
            field = value
            view?.findViewById<TextView>(R.id.textview_loading)?.setText(field)
        }

    var viewVisible: Boolean = true
        set(value) {
            field = value
            view?.visibility = if (!field) View.INVISIBLE else View.GONE
            updateWindowAttribute()
        }

    private val showRunnable = {
        try {
            show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_waiting)
        view = findViewById(R.id.layout_content)
        view!!.visibility = if (viewVisible) View.VISIBLE else View.GONE
        view?.findViewById<TextView>(R.id.textview_loading)!!.setText(text)
        updateWindowAttribute()
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }

    private fun updateWindowAttribute() {
        if (!viewVisible) {
            val lp = window!!.attributes
            lp.dimAmount = 0.2f
            window!!.attributes = lp
        }
    }


    fun postShow(delayMillis: Long) {
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed(showRunnable, delayMillis)
    }

    override fun show() {
        handler.removeCallbacksAndMessages(null)
        super.show()
    }

    override fun dismiss() {
        handler.removeCallbacksAndMessages(null)
        super.dismiss()
    }

}