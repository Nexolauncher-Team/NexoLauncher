package com.nexo.launcher.setting

import androidx.annotation.CheckResult
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.nexo.launcher.event.single.SettingsChangeEvent
import com.nexo.launcher.feature.log.Logging
import com.nexo.launcher.setting.unit.AbstractSettingUnit
import com.nexo.launcher.utils.path.PathManager
import com.nexo.launcher.Tools
import org.apache.commons.io.FileUtils
import org.greenrobot.eventbus.EventBus
import java.lang.reflect.Type
import java.util.concurrent.ConcurrentHashMap

class Settings {
    companion object {
        private val GSON: Gson = GsonBuilder().disableHtmlEscaping().create()

        private val settingsLock = Any()
        private var settingsMap = ConcurrentHashMap<String, SettingAttribute>()

        private fun refreshSettingsMap(): Map<String, SettingAttribute> {
            return PathManager.FILE_SETTINGS.takeIf { it.exists() }?.let { file ->
                try {
                    val jsonString = Tools.read(file)
                    val listType: Type = object : TypeToken<List<SettingAttribute>>() {}.type
                    GSON.fromJson<List<SettingAttribute>>(jsonString, listType)
                        .associateBy { it.key }
                } catch (e: Exception) {
                    Logging.e("Settings", "Failed to refresh settings: ${Tools.printToString(e)}")
                    emptyMap()
                }
            } ?: emptyMap()
        }

        /**
         * åˆ·æ–°å¯åŠ¨å™¨çš„æ‰€æœ‰è®¾ç½®é¡¹
         */
        @Synchronized
        fun refreshSettings() {
            settingsMap = ConcurrentHashMap(refreshSettingsMap())
        }
    }

    class Manager private constructor() {
        companion object {
            /**
             * åœ¨å¯åŠ¨å™¨è®¾ç½®ä¸­èŽ·å–é”®å¯¹åº”çš„å€¼
             */
            fun <T> getValue(key: String, defaultValue: T, parser: (String) -> T?): T {
                return settingsMap[key]?.value?.let { parser(it) } ?: defaultValue
            }

            /**
             * æ£€æŸ¥å¯åŠ¨å™¨è®¾ç½®ä¸­ï¼Œæ˜¯å¦å­˜åœ¨æŸä¸ªé”®
             */
            @JvmStatic
            fun contains(key: String): Boolean {
                return settingsMap.containsKey(key)
            }

            /**
             * åœ¨å¯åŠ¨å™¨è®¾ç½®ä¸­å­˜å…¥é”®å€¼
             */
            @JvmStatic
            @CheckResult
            fun put(key: String, value: Any) = SettingBuilder().put(key, value)
        }

        class SettingBuilder {
            private val valueMap = ConcurrentHashMap<String, Any>()

            /**
             * åœ¨å¯åŠ¨å™¨è®¾ç½®ä¸­å­˜å…¥é”®å€¼
             */
            @CheckResult
            fun put(key: String, value: Any): SettingBuilder {
                valueMap[key] = value
                return this
            }

            /**
             * åœ¨å¯åŠ¨å™¨è®¾ç½®ä¸­å­˜å…¥é”®å€¼
             * @param unit è®¾ç½®å•å…ƒ
             */
            @CheckResult
            fun put(unit: AbstractSettingUnit<*>, value: Any): SettingBuilder {
                return put(unit.key, value)
            }

            fun save() {
                val settingsFile = PathManager.FILE_SETTINGS
                val newSettings = ConcurrentHashMap(settingsMap)

                valueMap.forEach { (key, value) ->
                    newSettings[key] = SettingAttribute(key, value.toString())
                }

                synchronized(settingsLock) {
                    runCatching {
                        if (!settingsFile.exists() && !settingsFile.createNewFile()) {
                            throw IllegalStateException("Failed to create settings file")
                        }

                        val settingsList = newSettings.values.toList()
                        val json = GSON.toJson(settingsList)
                        FileUtils.write(settingsFile, json, Charsets.UTF_8)
                        refreshSettings()
                        EventBus.getDefault().post(SettingsChangeEvent())
                    }.onFailure { e ->
                        Logging.e("SettingBuilder", "Save failed!", e)
                    }
                }
            }
        }
    }

    private class SettingAttribute(
        var key: String = "",
        var value: String? = null
    )
}
