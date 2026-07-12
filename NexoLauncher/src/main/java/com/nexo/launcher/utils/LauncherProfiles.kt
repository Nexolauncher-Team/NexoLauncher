package com.nexo.launcher.utils

import com.nexo.launcher.feature.customprofilepath.ProfilePathHome
import com.nexo.launcher.feature.log.Logging
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException

class LauncherProfiles {
    companion object {
        /**
         * å†™å…¥ä¸€ä¸ªé»˜è®¤çš„ launcher_profiles.json æ–‡ä»¶ï¼Œä¸å­˜åœ¨å°†ä¼šå¯¼è‡´ Forgeã€NeoForge ç­‰æ— æ³•æ­£å¸¸å®‰è£…
         */
        @JvmStatic
        fun generateLauncherProfiles() {
            runCatching {
                File(ProfilePathHome.getGameHome(), "launcher_profiles.json").apply {
                    if (!exists()) {
                        if (parentFile?.exists() == false) parentFile?.mkdirs()
                        if (!createNewFile()) throw IOException("Failed to create launcher_profiles.json file!")
                        //å¼€å§‹å†™å…¥å†…å®¹
                        val profilesJsonString = """{"profiles":{"default":{"lastVersionId":"latest-release"}},"selectedProfile":"default"}""".trimIndent()
                        FileUtils.write(this, profilesJsonString)
                        Logging.i(
                            "Write launcher_profiles.json",
                            "The content has already been written! \r\nFile Location: $absolutePath\r\nContents: $profilesJsonString"
                        )
                    }
                }
            }.getOrElse { e ->
                Logging.e("Write launcher_profiles.json", "Unable to generate launcher_profiles.json file!", e)
            }
        }
    }
}
