package com.nexo.launcher.setting.unit

import com.nexo.launcher.setting.Settings.Manager

class FloatSettingUnit(key: String, defaultValue: Float) : AbstractSettingUnit<Float>(key, defaultValue) {
    override fun getValue() = Manager.getValue(key, defaultValue) { it.toFloatOrNull() }
}
