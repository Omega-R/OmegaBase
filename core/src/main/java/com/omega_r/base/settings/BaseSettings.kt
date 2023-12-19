package com.omega_r.base.settings

import com.omega_r.base.enitity.Percent
import kotlin.properties.ReadWriteProperty

open class BaseSettings(private val setting: SettingStorage) {

    protected fun provideBoolean(
        key: String,
        defaultValue: Boolean,
        label: String,
        description: String = ""
    ) = run {
        setting.provide(
            Setting.BooleanSetting(
                key = key,
                defaultValue = defaultValue,
                label = label,
                description = description
            )
        )
    }

    protected fun provideString(
        key: String,
        defaultValue: String,
        label: String,
        description: String = ""
    ) = run {
        setting.provide(
            Setting.StringSetting(
                key = key,
                defaultValue = defaultValue,
                label = label,
                description = description
            )
        )
    }

    protected fun providePercent(
        key: String,
        defaultValue: Percent,
        label: String,
        description: String = ""
    ) = run {
        setting.provide(
            Setting.PercentSetting(
                key = key,
                defaultValue = defaultValue,
                label = label,
                description = description
            )
        )
    }

}