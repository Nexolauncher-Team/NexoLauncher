package com.nexo.launcher.feature.download.install

import com.nexo.launcher.feature.log.Logging
import com.nexo.launcher.utils.ZipUtils
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.util.zip.ZipFile

class UnpackWorldZipHelper {
    companion object {
        fun unpackFile(zipFile: File, targetPath: File) {
            val path = extractLevelPath(zipFile) ?: throw IOException()
            Logging.i("UnpackWorldZipHelper", "Found the level of the level.data file: $path")
            ZipFile(zipFile).use {
                val fileName = zipFile.name.removeSuffix(".${zipFile.extension}")
                ZipUtils.zipExtract(ZipFile(zipFile), path, File(targetPath, fileName))
                Logging.i("UnpackWorldZipHelper", "Decompression is complete")
            }
            FileUtils.deleteQuietly(zipFile)
        }

        /**
         * è¯»å–zipæ–‡ä»¶ï¼Œå¹¶æ‰¾åˆ°level.dataæ–‡ä»¶æ‰€åœ¨çš„è·¯å¾„
         * @param file åŽ‹ç¼©åŒ…æ–‡ä»¶
         */
        private fun extractLevelPath(file: File): String? {
            if (!file.exists() || !file.isFile) {
                return null
            }

            if (!file.name.endsWith(".zip", ignoreCase = true)) {
                return null
            }

            ZipFile(file).use { zip ->
                val entries = zip.entries().asSequence() //è½¬æ¢ä¸ºåºåˆ—ï¼Œæ–¹ä¾¿è¿‡æ»¤
                val levelDatEntry = entries.find { it.name.endsWith("level.dat", ignoreCase = true) }
                if (levelDatEntry == null) {
                    return null
                }
                val path = levelDatEntry.name
                val levelPath = path.substringBeforeLast("/")
                return levelPath
            }
        }
    }
}
