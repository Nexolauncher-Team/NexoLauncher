package com.nexo.launcher.feature.unpack

import android.content.Context
import com.nexo.launcher.feature.log.Logging.e
import com.nexo.launcher.utils.CopyDefaultFromAssets.Companion.copyFromAssets
import com.nexo.launcher.utils.path.PathManager
import com.nexo.launcher.Tools

class UnpackSingleFilesTask(val context: Context) : AbstractUnpackTask() {
    override fun isNeedUnpack(): Boolean = true

    override fun run() {
        runCatching {
            copyFromAssets(context)
            Tools.copyAssetFile(context, "resolv.conf", PathManager.DIR_DATA, false)
        }.getOrElse { e("AsyncAssetManager", "Failed to unpack critical components !") }
    }
}
