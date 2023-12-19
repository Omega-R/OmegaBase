package com.omega_r.base.settings

import android.content.SharedPreferences
import androidx.core.content.edit
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SettingStorage(private val sharedPreferences: SharedPreferences){

    operator fun <T> get(setting: Setting<T>): T {
        return setting.getValue(sharedPreferences)
    }

    operator fun <T> set(setting: Setting<T>, newValue: T) {
         sharedPreferences.edit {
            setting.setValue(this, newValue)
         }
    }

    fun <T> provide(setting: Setting<T>): ReadWriteProperty<Any, T> = Property(setting)

    private inner class Property<T>(private val setting: Setting<T>): ReadWriteProperty<Any, T> {
        override fun getValue(thisRef: Any, property: KProperty<*>): T = get(setting)

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) = set(setting, value)

    }

}