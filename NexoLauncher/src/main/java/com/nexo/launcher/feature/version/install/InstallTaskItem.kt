package com.nexo.launcher.feature.version.install

import android.app.Activity
import java.io.File

/**
 * InstallTaskçš„åŒ…è£…ç±»ï¼Œç”¨äºŽè®°å½•æ›´è¯¦ç»†çš„ä¿¡æ¯
 * @see InstallTask
 */
class InstallTaskItem(
    val selectedVersion: String,
    val isMod: Boolean,
    val task: InstallTask,
    val endTask: EndTask?
) {
    override fun toString(): String {
        return "InstallTaskItem{selectedVersion='$selectedVersion', isMod='$isMod'}"
    }

    fun interface EndTask {
        /**
         * ä½¿ç”¨è¿™ä¸ªä»»åŠ¡æ‰§è¡ŒModLoaderçš„å®‰è£…
         * @param activity å½“å‰çš„Activityï¼Œç”¨æ¥è°ƒå‡ºjreé€‰æ‹©å¼¹çª—ã€åˆ‡æ¢è‡³JavaGUIç•Œé¢
         * @param file ä¸Šä¸€ä¸ªä»»åŠ¡æ‰§è¡Œå®ŒæˆåŽè¾“å‡ºçš„æ–‡ä»¶
         */
        @Throws(Throwable::class)
        fun endTask(activity: Activity, file: File)
    }
}
