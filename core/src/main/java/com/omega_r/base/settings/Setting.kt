package com.omega_r.base.settings

import android.content.SharedPreferences
import com.omega_r.base.enitity.Percent
import com.omega_r.base.enitity.PercentModel
import com.omega_r.base.enitity.toPercent

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

}