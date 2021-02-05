package com.omega_r.base.remote.adapters

import android.content.Context
import androidx.core.text.HtmlCompat
import com.omega_r.libs.omegatypes.Text
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter

/**
 * Created by Anton Knyazev on 2019-05-28.
 */
class HtmlTextAdapter(private val context: Context?, private val flags: Int) : JsonAdapter<Text>() {

    override fun fromJson(reader: JsonReader): Text {
        return Text.from(HtmlCompat.fromHtml(reader.nextString(), flags))
    }

    override fun toJson(writer: JsonWriter, value: Text?) {
        context?.let {
            writer.value(value?.getString(it))
        } ?: throw UnsupportedOperationException("Method toJson not supported")

    }

}