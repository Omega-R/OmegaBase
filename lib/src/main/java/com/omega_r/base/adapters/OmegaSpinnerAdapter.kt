package com.omega_r.base.adapters

import android.content.Context
import android.widget.AdapterView.INVALID_POSITION
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.annotation.LayoutRes
import java.util.*

/**
 * Created by Anton Knyazev on 2019-07-02.
 */

abstract class OmegaSpinnerAdapter<M>(
    context: Context,
    @LayoutRes res: Int = android.R.layout.simple_spinner_item,
    list: List<M> = Collections.emptyList()
) : ArrayAdapter<CharSequence>(context, res, Collections.emptyList()) {

    var list: List<M> = list
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var nonSelectedItem: M? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    abstract fun getItemName(item: M): CharSequence

    override fun getItem(position: Int): CharSequence? {
        if (nonSelectedItem == null) {
            return getItemName(list[position])
        } else if (position == 0) {
            return getItemName(nonSelectedItem!!)
        } else {
            return getItemName(list[position - 1])
        }
    }

    override fun getCount(): Int = list.size + (if (nonSelectedItem == null) 0 else 1)

    fun setSelection(spinner: Spinner, item: M?) {
        if (item == null) {
            setSelection(spinner, 0)
            return
        }
        for (i in list.indices) {
            val position = if (nonSelectedItem == null) i else i + 1
            if (list[i] == item) {
                setSelection(spinner, position)
                return
            }
        }
    }

    private fun setSelection(spinner: Spinner, position: Int) {
        if (spinner.selectedItemPosition == position) return
        spinner.setSelection(position)
    }

    fun getSelection(spinner: Spinner): M? {
        val position = getSelectionPosition(spinner)
        return if (position >= 0) list[position] else null
    }

    fun getSelectionPosition(spinner: Spinner): Int {
        val position = spinner.selectedItemPosition
        nonSelectedItem?.let {
            if (position <= 0) return INVALID_POSITION
            return position - 1
        }
        return position
    }

    class Text(
        context: Context,
        res: Int = android.R.layout.simple_spinner_item,
        list: List<com.omega_r.libs.omegatypes.Text> = emptyList()
    ) :
        OmegaSpinnerAdapter<com.omega_r.libs.omegatypes.Text>(context, res, list) {

        override fun getItemName(item: com.omega_r.libs.omegatypes.Text) = item.getCharSequence(context) ?: ""

    }

    class String(
        context: Context,
        res: Int = android.R.layout.simple_spinner_item,
        list: List<kotlin.String> = emptyList()
    ) :
        OmegaSpinnerAdapter<kotlin.String>(context, res, list) {

        override fun getItemName(item: kotlin.String): CharSequence = item
    }

    class Custom<M>(
        context: Context,
        res: Int = android.R.layout.simple_spinner_item,
        private val converter: (Context, M) -> CharSequence,
        list: List<M> = emptyList()
    ) :
        OmegaSpinnerAdapter<M>(context, res, list) {

        override fun getItemName(item: M): CharSequence = converter(context, item)
    }


}