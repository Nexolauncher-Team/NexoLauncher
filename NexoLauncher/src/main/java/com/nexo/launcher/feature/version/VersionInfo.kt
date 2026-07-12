package com.nexo.launcher.feature.version

import com.nexo.launcher.feature.log.Logging
import com.nexo.launcher.Tools
import java.io.File
import java.io.FileWriter

class VersionInfo(
    val minecraftVersion: String,
    val loaderInfo: Array<LoaderInfo>?
) {
    /**
     * æ‹¼æŽ¥Minecraftçš„ç‰ˆæœ¬ä¿¡æ¯ï¼ŒåŒ…æ‹¬ModLoaderä¿¡æ¯
     * @return ç”¨", "åˆ†å‰²çš„ä¿¡æ¯å­—ç¬¦ä¸²
     */
    fun getInfoString(): String {
        val infoList = mutableListOf<String>().apply {
            add(minecraftVersion)
            loaderInfo?.forEach { info ->
                when {
                    info.name.isNotBlank() && info.version.isNotBlank() -> add("${info.name} - ${info.version}")
                    info.name.isNotBlank() -> add(info.name)
                    info.version.isNotBlank() -> add(info.version)
                }
            }
        }
        return infoList.joinToString(", ")
    }

    data class LoaderInfo(
        val name: String,
        val version: String
    ) {
        /**
         * é€šè¿‡åŠ è½½å™¨åç§°ï¼ŒèŽ·å¾—å¯¹åº”çš„çŽ¯å¢ƒå˜é‡é”®å
         */
        fun getLoaderEnvKey(): String? {
            return when(name) {
                "OptiFine" -> "INST_OPTIFINE"
                "Forge" -> "INST_FORGE"
                "NeoForge" -> "INST_NEOFORGE"
                "Fabric" -> "INST_FABRIC"
                "Quilt" -> "INST_QUILT"
                "LiteLoader" -> "INST_LITELOADER"
                else -> null
            }
        }
    }

    fun save(versionFolder: File) {
        runCatching {
            val nexoVersionPath = VersionsManager.getNexoVersionPath(versionFolder)
            val infoFile = File(nexoVersionPath, "VersionInfo.json")
            if (!nexoVersionPath.exists()) nexoVersionPath.mkdirs()

            FileWriter(infoFile, false).use {
                val json = Tools.GLOBAL_GSON.toJson(this)
                it.write(json)
            }
        }.onFailure { e -> Logging.e("Save Version Info", Tools.printToString(e)) }
    }
}
