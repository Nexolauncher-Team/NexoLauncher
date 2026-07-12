package com.nexo.launcher.context

import android.content.Context
import android.content.ContextWrapper
import com.nexo.launcher.setting.Settings
import com.nexo.launcher.utils.path.PathManager
import com.nexo.launcher.prefs.LauncherPreferences

class LocaleHelper(context: Context) : ContextWrapper(context) {
    companion object {
        fun setLocale(context: Context): ContextWrapper {
            //åˆå§‹åŒ–è·¯å¾„
            PathManager.initContextConstants(context)
            //åˆ·æ–°å¯åŠ¨å™¨è®¾ç½®
            Settings.refreshSettings()

            LauncherPreferences.loadPreferences()
            return LocaleHelper(context)
        }
    }
}
