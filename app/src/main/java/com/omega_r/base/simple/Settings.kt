package com.omega_r.base.simple

import com.omega_r.base.settings.BaseSettings
import com.omega_r.base.settings.SettingStorage

class Settings(setting: SettingStorage): BaseSettings(setting) {

    var firstLaunch by provideBoolean(key = "jklh", defaultValue = false, label = "possim")


}