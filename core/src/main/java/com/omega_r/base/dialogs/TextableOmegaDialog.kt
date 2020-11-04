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
 * Created by Nikita Ivanov on 04.11.2020.
 */
abstract class TextableOmegaDialog(context: Context) : OmegaDialog(context) {

    abstract var text: Text
    
}