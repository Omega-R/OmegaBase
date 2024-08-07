package com.omega_r.base.settings

import com.omega_r.base.enitity.Percent
import com.squareup.moshi.JsonAdapter
import java.util.Date
import kotlin.time.Duration

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

    protected fun provideInt(
        key: String,
        defaultValue: Int,
        label: String,
        description: String = ""
    ) = run {
        setting.provide(
            Setting.IntSetting(
                key = key,
                defaultValue = defaultValue,
                label = label,
                description = description
            )
        )
    }

    protected fun provideFloat(
        key: String,
        defaultValue: Float,
        label: String,
        description: String = ""
    ) = run {
        setting.provide(
            Setting.FloatSetting(
                key = key,
                defaultValue = defaultValue,
                label = label,
                description = description
            )
        )
    }

    protected fun provideLong(
        key: String,
        defaultValue: Long,
        label: String,
        description: String = ""
    ) = run {
        setting.provide(
            Setting.LongSetting(
                key = key,
                defaultValue = defaultValue,
                label = label,
                description = description
            )
        )
    }

    protected fun provideDouble(
        key: String,
        defaultValue: Double,
        label: String,
        description: String = ""
    ) = run {
        setting.provide(
            Setting.DoubleSetting(
                key = key,
                defaultValue = defaultValue,
                label = label,
                description = description
            )
        )
    }

    protected fun provideDate(
        key: String,
        defaultValue: Date,
        label: String,
        description: String = ""
    ) = run {
        setting.provide(
            Setting.DateSetting(
                key = key,
                defaultValue = defaultValue,
                label = label,
                description = description
            )
        )
    }

    protected fun provideDuration(
        key: String,
        defaultValue: Duration,
        label: String,
        description: String = ""
    ) = run {
        setting.provide(
            Setting.DurationSetting(
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


    protected fun <T: Any?> provideAnyJson(
        key: String,
        defaultValue: T,
        label: String,
        jsonAdapter: JsonAdapter<T>,
        description: String = "",
    ) = run {
        setting.provide(
            Setting.AnyJsonSetting(
                key = key,
                defaultValue = defaultValue,
                label = label,
                description = description,
                jsonAdapter = jsonAdapter
            )
        )
    }

}