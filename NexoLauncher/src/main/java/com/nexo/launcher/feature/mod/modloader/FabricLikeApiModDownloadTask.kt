package com.nexo.launcher.feature.mod.modloader

import com.nexo.launcher.mcgui.ProgressLayout
import com.nexo.launcher.R
import com.nexo.launcher.feature.download.item.VersionItem
import com.nexo.launcher.feature.version.install.InstallTask
import com.nexo.launcher.utils.path.PathManager
import com.nexo.launcher.Tools
import com.nexo.launcher.progresskeeper.ProgressKeeper
import com.nexo.launcher.utils.DownloadUtils
import java.io.File

class FabricLikeApiModDownloadTask(private val fileName: String, private val versionItem: VersionItem) : InstallTask, Tools.DownloaderFeedback {
    @Throws(Exception::class)
    override fun run(customName: String): File {
        ProgressKeeper.submitProgress(ProgressLayout.INSTALL_RESOURCE, 0, R.string.mod_download_progress, versionItem.fileName)
        val destinationFile = File(PathManager.DIR_CACHE, "$fileName.jar")
        DownloadUtils.downloadFileMonitored(versionItem.fileUrl, destinationFile, ByteArray(8192), this)
        ProgressLayout.clearProgress(ProgressLayout.INSTALL_RESOURCE)
        return destinationFile
    }

    override fun updateProgress(curr: Long, max: Long) {
        val progress100 = ((curr.toFloat() / max.toFloat()) * 100f).toInt()
        ProgressKeeper.submitProgress(ProgressLayout.INSTALL_RESOURCE, progress100, R.string.mod_download_progress, versionItem.fileName)
    }
}
