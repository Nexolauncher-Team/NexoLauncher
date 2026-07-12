package com.nexo.launcher.feature.version

import com.google.gson.annotations.SerializedName
import com.nexo.launcher.feature.customprofilepath.ProfilePathHome
import com.nexo.launcher.feature.log.Logging
import com.nexo.launcher.Tools
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * å½“å‰æ¸¸æˆçŠ¶æ€ä¿¡æ¯ï¼ˆæ”¯æŒæ—§é…ç½®è¿ç§»ï¼‰
 * @property version å½“å‰é€‰æ‹©çš„ç‰ˆæœ¬åç§°
 * @property favoritesMap æ”¶è—å¤¹æ˜ å°„è¡¨ <æ”¶è—å¤¹åç§°, åŒ…å«çš„ç‰ˆæœ¬é›†åˆ>
 */
data class CurrentGameInfo(
    @SerializedName("version")
    var version: String = "",
    @SerializedName("favoritesInfo")
    val favoritesMap: MutableMap<String, MutableSet<String>> = ConcurrentHashMap()
) {
    /**
     * åŽŸå­åŒ–ä¿å­˜å½“å‰çŠ¶æ€åˆ°æ–‡ä»¶
     */
    fun saveCurrentInfo() {
        val infoFile = getInfoFile()
        runCatching {
            FileUtils.writeByteArrayToFile(
                infoFile,
                Tools.GLOBAL_GSON.toJson(this).toByteArray(Charsets.UTF_8)
            )
        }.onFailure { e ->
            Logging.e("CurrentGameInfo", "Save failed: ${infoFile.absolutePath}", e)
        }
    }

    companion object {
        private fun getInfoFile() = File(ProfilePathHome.getGameHome(), "CurrentInfo.cfg")

        private fun getLegacyInfoFile() = File(ProfilePathHome.getGameHome(), "CurrentVersion.cfg")

        /**
         * åˆ·æ–°å¹¶è¿”å›žæœ€æ–°çš„æ¸¸æˆä¿¡æ¯ï¼ˆè‡ªåŠ¨å¤„ç†æ—§é…ç½®è¿ç§»ï¼‰
         */
        fun refreshCurrentInfo(): CurrentGameInfo {
            val infoFile = getInfoFile()
            val legacyInfoFile = getLegacyInfoFile()

            return try {
                when {
                    infoFile.exists() -> loadFromJsonFile(infoFile)
                    legacyInfoFile.exists() -> migrateLegacyConfig(legacyInfoFile)
                    else -> createNewConfig()
                }
            } catch (e: Exception) {
                Logging.e("CurrentGameInfo", "Refresh failed", e)
                createNewConfig()
            }
        }

        private fun loadFromJsonFile(infoFile: File): CurrentGameInfo {
            return Tools.GLOBAL_GSON.fromJson(infoFile.readText(), CurrentGameInfo::class.java)
                .also { info -> checkNotNull(info) { "Deserialization returned null" } }
        }

        private fun migrateLegacyConfig(infoFile: File): CurrentGameInfo {
            return CurrentGameInfo().apply {
                version = infoFile.takeIf { it.exists() }?.readText() ?: ""
                infoFile.delete()
            }.applyPostActions()
        }

        private fun createNewConfig() = CurrentGameInfo().applyPostActions()

        private fun CurrentGameInfo.applyPostActions(): CurrentGameInfo {
            saveCurrentInfo()
            return this
        }
    }
}
