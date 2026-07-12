package com.nexo.launcher.feature.download.utils

import com.nexo.launcher.InfoDistributor
import com.nexo.launcher.feature.download.Filters
import com.nexo.launcher.feature.download.enums.Classify
import com.nexo.launcher.utils.stringutils.StringUtils.containsChinese
import com.nexo.launcher.utils.stringutils.StringUtilsKt
import com.nexo.launcher.modloaders.modpacks.api.ApiHandler
import org.jackhuang.hmcl.ui.versions.ModTranslations
import org.jackhuang.hmcl.util.StringUtils

class PlatformUtils {
    companion object {
        fun createCurseForgeApi() = ApiHandler(
            "https://api.curseforge.com/v1",
            InfoDistributor.CURSEFORGE_API_KEY
        )

        /**
         * ä¿®æ”¹è‡ªæºä»£ç ï¼š[HMCL Github](https://github.com/HMCL-dev/HMCL/blob/main/HMCL/src/main/java/org/jackhuang/hmcl/game/LocalizedRemoteModRepository.java#L44-#L104)
         * åŽŸé¡¹ç›®ç‰ˆæƒå½’åŽŸä½œè€…æ‰€æœ‰ï¼Œéµå¾ªGPL v3åè®®
         */
        fun searchModLikeWithChinese(
            filters: Filters,
            isMod: Boolean
        ): String? {
            if (!containsChinese(filters.name)) return null
            val classify = if (isMod) Classify.MOD else Classify.MODPACK

            val englishSearchFiltersSet: MutableSet<String> = HashSet(16)

            for ((count, mod) in ModTranslations.getTranslationsByRepositoryType(classify)
                .searchMod(filters.name).withIndex()
            ) {
                for (englishWord in StringUtils.tokenize(if (StringUtilsKt.isNotBlank(mod.subname)) mod.subname else mod.name)) {
                    if (englishSearchFiltersSet.contains(englishWord)) continue
                    englishSearchFiltersSet.add(englishWord)
                }
                if (count >= 3) break
            }

            // TODO ç”±äºŽæœç´¢é€»è¾‘ä¸ŽHMCLå¤§ä¸ç›¸åŒï¼Œè¿™é‡Œå°±ä¸åšè¿›ä¸€æ­¥çš„ç­›æŸ¥é€»è¾‘äº†ï¼Œç›´æŽ¥è¿”å›žæœ¬åœ°åŒ¹é…ç»“æžœï¼Œä½œä¸ºå¹³å°çš„æœç´¢å…³é”®è¯ï¼Œä¸è¿‡æ— æ³•ä¿è¯ç»“æžœçš„å‡†ç¡®åº¦
            return englishSearchFiltersSet.joinToString(" ")
        }

        inline fun <T> ApiHandler.safeRun(block: ApiHandler.() -> T): T? =
            runCatching(block).getOrNull()
    }
}
