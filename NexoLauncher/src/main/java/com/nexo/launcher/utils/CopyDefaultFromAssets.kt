package com.nexo.launcher.utils

import android.content.Context
import com.nexo.launcher.utils.path.PathManager
import com.nexo.launcher.Tools
import java.io.File
import java.io.IOException

class CopyDefaultFromAssets {
    companion object {
        @JvmStatic
        @Throws(IOException::class)
        fun copyFromAssets(context: Context?) {
            //é»˜è®¤æŽ§åˆ¶å¸ƒå±€
            if (checkDirectoryEmpty(PathManager.DIR_CTRLMAP_PATH)) {
                Tools.copyAssetFile(context, "default.json", PathManager.DIR_CTRLMAP_PATH, false)
            }
        }

        private fun checkDirectoryEmpty(dir: String?): Boolean {
            val controlDir = dir?.let { File(it) }
            val files = controlDir?.listFiles()
            return files?.isEmpty() ?: true
        }
    }
}

