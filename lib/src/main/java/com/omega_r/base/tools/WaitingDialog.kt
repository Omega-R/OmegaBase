package com.omega_r.base.tools

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
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
    private var windowBackground: Drawable? = null

    var text: Text = Text.from(R.string.loading)
        set(value) {
            field = value
            view?.findViewById<TextView>(R.id.textview_loading)?.setText(field)
        }

    private val showRunnable = Runnable {
        try {
            window?.setBackgroundDrawable(windowBackground)
            window?.setDimAmount(0.65f)
            view?.visibility = View.VISIBLE
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_waiting)
        view = findViewById(R.id.layout_content)
        view?.findViewById<TextView>(R.id.textview_loading)!!.setText(text)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        windowBackground = window?.decorView?.background
    }

    fun postShow(delayMillis: Long) {
        super.show()
        window?.setDimAmount(0f)
        handler.removeCallbacksAndMessages(null)
        view?.visibility = View.INVISIBLE
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        handler.postDelayed(showRunnable, delayMillis)
    }

    override fun show() {
        handler.removeCallbacks(showRunnable)
        showRunnable.run()
        super.show()
    }

    override fun dismiss() {
        handler.removeCallbacks(showRunnable)
        super.dismiss()
    }

}