package com.omega_r.base.simple

import com.omega_r.base.settings.BaseSettings
import com.omega_r.base.settings.SettingStorage
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter

class Settings(setting: SettingStorage, moshi: Moshi): BaseSettings(setting) {

    var firstLaunch by provideBoolean(key = "jklh", defaultValue = false, label = "possim")

    @OptIn(ExperimentalStdlibApi::class)
    var session: Session? by provideAnyJson(key = "", defaultValue = null, label = "sdf", moshi.adapter())

    class Session(val token: String, val refreshToken: String)

}