package com.nexo.launcher.plugins.driver

import android.content.Context
import android.content.pm.ApplicationInfo
import com.nexo.launcher.setting.AllSettings

/**
 * FCL é©±åŠ¨å™¨æ’ä»¶
 * [FCL DriverPlugin.kt](https://github.com/FCL-Team/FoldCraftLauncher/blob/main/FCLauncher/src/main/java/com/tungsten/fclauncher/plugins/DriverPlugin.kt)
 */
object DriverPluginManager {
    private val driverList: MutableList<Driver> = mutableListOf()

    @JvmStatic
    fun getDriverNameList(): List<String> = driverList.map { it.driver }

    private lateinit var currentDriver: Driver

    @JvmStatic
    fun setDriverByName(driverName: String) {
        currentDriver = driverList.find { it.driver == driverName } ?: driverList[0]
    }

    @JvmStatic
    fun getDriver(): Driver = currentDriver

    /**
     * åˆå§‹åŒ–é©±åŠ¨å™¨
     * @param reset æ˜¯å¦æ¸…é™¤å·²æœ‰æ’ä»¶
     */
    fun initDriver(context: Context, reset: Boolean) {
        if (reset) driverList.clear()
        driverList.add(Driver("Turnip", context.applicationInfo.nativeLibraryDir))
        setDriverByName(AllSettings.driver.getValue())
    }

    /**
     * é€šç”¨ FCL æ’ä»¶
     */
    fun parsePlugin(info: ApplicationInfo) {
        if (info.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
            val metaData = info.metaData ?: return
            if (metaData.getBoolean("fclPlugin", false)) {
                val driver = metaData.getString("driver") ?: return
                val nativeLibraryDir = info.nativeLibraryDir
                driverList.add(
                    Driver(
                        driver,
                        nativeLibraryDir
                    )
                )
            }
        }
    }
}
