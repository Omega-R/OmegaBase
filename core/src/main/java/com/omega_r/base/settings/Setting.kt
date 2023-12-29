package com.omega_r.base.settings

import android.content.SharedPreferences
import com.omega_r.base.enitity.Percent
import com.omega_r.base.enitity.PercentModel
import com.omega_r.base.enitity.toPercent
import com.squareup.moshi.JsonAdapter
import java.util.Date
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

sealed class Setting<T>(
    val key: String,
    val defaultValue: T,
    val label: String,
    val description: String
) {

    internal abstract fun getValue(sharedPreferences: SharedPreferences): T

    internal abstract fun setValue(editor: SharedPreferences.Editor, value: T)

    open class BooleanSetting(key: String, defaultValue: Boolean, label: String, description: String) :
        Setting<Boolean>(key = key, defaultValue = defaultValue, label = label, description = description) {

        override fun getValue(sharedPreferences: SharedPreferences): Boolean {
            return sharedPreferences.getBoolean(key, defaultValue)
        }

        override fun setValue(editor: SharedPreferences.Editor, value: Boolean) {
            editor.putBoolean(key, value)
        }

    }

    open class IntSetting(key: String, defaultValue: Int, label: String, description: String) :
        Setting<Int>(key = key, defaultValue = defaultValue, label = label, description = description) {
        override fun getValue(sharedPreferences: SharedPreferences): Int {
            return sharedPreferences.getInt(key, defaultValue)
        }

        override fun setValue(editor: SharedPreferences.Editor, value: Int) {
            editor.putInt(key, value)
        }
    }

    open class FloatSetting(key: String, defaultValue: Float, label: String, description: String) :
        Setting<Float>(key = key, defaultValue = defaultValue, label = label, description = description) {
        override fun getValue(sharedPreferences: SharedPreferences): Float {
            return sharedPreferences.getFloat(key, defaultValue)
        }

        override fun setValue(editor: SharedPreferences.Editor, value: Float) {
            editor.putFloat(key, value)
        }
    }

    open class LongSetting(key: String, defaultValue: Long, label: String, description: String) :
        Setting<Long>(key = key, defaultValue = defaultValue, label = label, description = description) {
        override fun getValue(sharedPreferences: SharedPreferences): Long {
            return sharedPreferences.getLong(key, defaultValue)
        }

        override fun setValue(editor: SharedPreferences.Editor, value: Long) {
            editor.putLong(key, value)
        }
    }

    open class DoubleSetting(key: String, defaultValue: Double, label: String, description: String) :
        Setting<Double>(key = key, defaultValue = defaultValue, label = label, description = description) {
        override fun getValue(sharedPreferences: SharedPreferences): Double {
            return java.lang.Double.longBitsToDouble(
                sharedPreferences.getLong(
                    key,
                    java.lang.Double.doubleToLongBits(defaultValue)
                )
            )
        }

        override fun setValue(editor: SharedPreferences.Editor, value: Double) {
            editor.putLong(key, java.lang.Double.doubleToLongBits(value))
        }
    }

    open class DateSetting(key: String, defaultValue: Date, label: String, description: String) :
        Setting<Date>(key = key, defaultValue = defaultValue, label = label, description = description) {
        override fun getValue(sharedPreferences: SharedPreferences): Date {
            return Date(sharedPreferences.getLong(key, defaultValue.time))
        }

        override fun setValue(editor: SharedPreferences.Editor, value: Date) {
            editor.putLong(key, value.time)
        }
    }

    open class DurationSetting(key: String, defaultValue: Duration, label: String, description: String) :
        Setting<Duration>(key = key, defaultValue = defaultValue, label = label, description = description) {
        override fun getValue(sharedPreferences: SharedPreferences): Duration {
            return sharedPreferences.getLong(key, defaultValue.inWholeMilliseconds).milliseconds
        }

        override fun setValue(editor: SharedPreferences.Editor, value: Duration) {
            editor.putLong(key, value.inWholeMilliseconds)
        }
    }

    open class StringSetting(key: String, defaultValue: String, label: String, description: String) :
        Setting<String>(key = key, defaultValue = defaultValue, label = label, description = description) {
        override fun getValue(sharedPreferences: SharedPreferences): String {
            return sharedPreferences.getString(key, defaultValue) ?: defaultValue
        }

        override fun setValue(editor: SharedPreferences.Editor, value: String) {
            editor.putString(key, value)
        }
    }

    open class PercentSetting(key: String, defaultValue: Percent, label: String, description: String) :
        Setting<Percent>(key = key, defaultValue = defaultValue, label = label, description = description) {

        private companion object {
            private val percentModel = PercentModel(0f, 100f)
        }

        override fun getValue(sharedPreferences: SharedPreferences): Percent {
            return sharedPreferences.getFloat(key, defaultValue.toFloat(percentModel)).toPercent(percentModel)
        }

        override fun setValue(editor: SharedPreferences.Editor, value: Percent) {
            editor.putFloat(key, value.toFloat(percentModel))
        }
    }


    open class AnyJsonSetting<T : Any?>(
        key: String,
        defaultValue: T,
        label: String,
        description: String,
        private val jsonAdapter: JsonAdapter<T>
    ) : Setting<T>(key = key, defaultValue = defaultValue, label = label, description = description) {

        private companion object {
            private const val DEFAULT_VALUE = "default_value"
        }

        override fun getValue(sharedPreferences: SharedPreferences): T {
            val json = sharedPreferences.getString(key, DEFAULT_VALUE)
            return if (json == DEFAULT_VALUE) defaultValue else json?.let { jsonAdapter.fromJson(json) } as T
        }

        override fun setValue(editor: SharedPreferences.Editor, value: T) {
            editor.putString(key, jsonAdapter.toJson(value))
        }
    }

}