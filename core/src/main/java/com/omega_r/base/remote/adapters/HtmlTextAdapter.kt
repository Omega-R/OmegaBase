package com.omega_r.base.remote.adapters

import android.content.Context
import android.text.SpannedString
import androidx.core.text.HtmlCompat
import com.omega_r.libs.omegatypes.Text
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter

/**
 * Created by Anton Knyazev on 2019-05-28.
 */
class HtmlTextAdapter(private val context: Context?, private val fromHtmlFlags: Int, private val toHtmlOptions: Int) :
    JsonAdapter<Text>() {

    override fun fromJson(reader: JsonReader): Text {
        return Text.from(HtmlCompat.fromHtml(reader.nextString(), fromHtmlFlags))
    }

    override fun toJson(writer: JsonWriter, value: Text?) {
        context?.let {
            value?.getCharSequence(it)?.let { charSequence ->
                when (charSequence) {
                    is SpannedString -> {
                        writer.value(HtmlCompat.toHtml(charSequence, toHtmlOptions));
                    }
                    is String -> {
                        writer.value(charSequence)
                    }
                    else -> {
                        throw UnsupportedOperationException("Text with ${charSequence::class.simpleName} not supported")
                    }
                }
            } ?: writer.nullValue()

        } ?: throw UnsupportedOperationException("Method toJson not supported")

    }

}