package com.nexo.launcher.setting.unit

import androidx.annotation.CheckResult
import com.nexo.launcher.setting.Settings

abstract class AbstractSettingUnit<V>(
    val key: String,
    val defaultValue: V
) {
    /**
     * @return èŽ·å–å½“å‰çš„è®¾ç½®å€¼
     */
    abstract fun getValue(): V

    /**
     * @return å­˜å…¥å€¼ï¼Œå¹¶è¿”å›žä¸€ä¸ªè®¾ç½®æž„å»ºå™¨
     */
    @CheckResult
    fun put(value: V): Settings.Manager.SettingBuilder = Settings.Manager.put(key, value!!)

    /**
     * é‡ç½®å½“å‰è®¾ç½®å•å…ƒä¸ºé»˜è®¤å€¼
     */
    fun reset() {
        Settings.Manager.put(key, defaultValue!!).save()
    }
}
