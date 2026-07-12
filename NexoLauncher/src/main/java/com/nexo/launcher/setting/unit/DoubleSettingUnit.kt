package com.nexo.launcher.setting.unit

import com.nexo.launcher.setting.Settings.Manager

class DoubleSettingUnit(key: String, defaultValue: Double) : AbstractSettingUnit<Double>(key, defaultValue) {
    override fun getValue() = Manager.getValue(key, defaultValue) { it.toDoubleOrNull() }
}
