package com.nexo.launcher.utils

import android.content.Context
import android.widget.Toast
import com.nexo.launcher.R
import com.nexo.launcher.feature.log.Logging
import com.nexo.launcher.task.Task
import com.nexo.launcher.task.TaskExecutors
import com.nexo.launcher.utils.file.FileTools
import com.nexo.launcher.utils.path.PathManager
import org.apache.commons.io.FileUtils
import java.io.File

class CleanUpCache {
    companion object {
        private var isCleaning = false

        @JvmStatic
        fun start(context: Context) {
            if (isCleaning) return
            isCleaning = true

            Task.runTask {
                cleanSync()
            }.ended(TaskExecutors.getAndroidUI()) { totalSize ->
                val size = totalSize ?: 0L
                if (size > 0) {
                    Toast.makeText(context,
                        context.getString(R.string.clear_up_cache_clean_up, FileTools.formatFileSize(size)),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(context,
                        context.getString(R.string.clear_up_cache_not_found),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                isCleaning = false
            }.execute()
        }

        @JvmStatic
        fun cleanSync(): Long {
            var totalSize: Long = 0
            try {
                val list = getList(
                    PathManager.DIR_CACHE.listFiles() ?: emptyArray(),
                    PathManager.DIR_APP_CACHE.listFiles() ?: emptyArray()
                )

                val versionListFile = File(PathManager.FILE_VERSION_LIST)
                if (versionListFile.exists()) list.add(versionListFile)

                for (file in list) {
                    totalSize += FileUtils.sizeOf(file)
                    FileUtils.deleteQuietly(file)
                }
            } catch (e: Exception) {
                Logging.e("CleanUpCache", "Error during sync cleanup", e)
            }
            return totalSize
        }

        private fun getList(vararg filesArray: Array<File>): MutableList<File> {
            val filesList: MutableList<File> = ArrayList()
            for (fileArray in filesArray) {
                filesList.addAll(listOf(*fileArray))
            }

            return filesList
        }
    }
}

