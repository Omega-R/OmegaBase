package com.omega_r.base.dialogs

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.omega_r.base.R
import com.omega_r.base.components.OmegaDialog
import com.omega_r.libs.omegatypes.Text
import com.omega_r.libs.omegatypes.setText


/**
 * Created by Anton Knyazev on 04.04.2019.
 */
class WaitingDialog(context: Context) : TextableOmegaDialog(context) {

    private var view: View? = null

    override var text: Text = Text.from(R.string.loading)
        set(value) {
            field = value
            view?.findViewById<TextView>(R.id.textview_loading)?.setText(field)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_waiting)
        view = findViewById(R.id.layout_content)
        view?.findViewById<TextView>(R.id.textview_loading)!!.setText(text)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }

}