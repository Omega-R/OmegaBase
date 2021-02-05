package com.omega_r.base.remote.adapters

import com.omega_r.libs.omegatypes.image.Image
import com.omega_r.libs.omegatypes.image.UrlImage
import com.omega_r.libs.omegatypes.image.from
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter

/**
 * Created by Anton Knyazev on 2019-05-28.
 */

class ImageAdapter(private val baseUrl: String? = null): JsonAdapter<Image>() {

    override fun fromJson(reader: JsonReader): Image? {
        return UrlImage(baseUrl, reader.nextString())
    }

    override fun toJson(writer: JsonWriter, value: Image?) {
        writer.value((value as UrlImage).url)
    }

}