package com.nexo.launcher.utils.stringutils

import java.util.UUID

class StringUtilsKt {
    companion object {
        @JvmStatic
        fun getNonEmptyOrBlank(string: String?): String? {
            return string?.takeIf { it.isNotEmpty() && it.isNotBlank() }
        }

        @JvmStatic
        fun isBlank(string: String?): Boolean = string.isNullOrBlank()

        @JvmStatic
        fun isNotBlank(string: String?): Boolean = string?.isNotBlank() ?: false

        @JvmStatic
        fun isEmptyOrBlank(string: String): Boolean = string.isEmpty() || string.isBlank()

        @JvmStatic
        fun removeSuffix(string: String, suffix: String) = string.removeSuffix(suffix)

        @JvmStatic
        fun removePrefix(string: String, prefix: String) = string.removePrefix(prefix)

        @JvmStatic
        fun decodeUnicode(input: String): String {
            val regex = """\\u([0-9a-fA-F]{4})""".toRegex()
            var result = input
            regex.findAll(input).forEach { match ->
                val unicode = match.groupValues[1]
                val char = Character.toChars(unicode.toInt(16))[0]
                result = result.replace(match.value, char.toString())
            }
            return result
        }

        /**
         * ç”Ÿæˆä¸€ä¸ªå”¯ä¸€UUIDï¼Œä»¥åŠé˜²æ­¢ä¸Žå·²å­˜åœ¨çš„UUIDå†²çª
         * @param processString è‹¥éœ€è¦æ“ä½œå­—ç¬¦ä¸²ï¼Œå¯ä»¥ä½¿ç”¨å®ƒ
         * @param checkForConflict è‹¥éœ€è¦é˜²æ­¢ä¸Žå·²å­˜åœ¨çš„UUIDå†²çªï¼Œå¯ä»¥ç”¨å®ƒæ£€æŸ¥æ˜¯å¦æœ‰å†²çªï¼Œå¦‚æžœè¿”å›žtrueï¼Œåˆ™é€’å½’é‡æ–°ç”Ÿæˆä¸€ä¸ª
         */
        @JvmStatic
        fun generateUniqueUUID(
            processString: ((String) -> String)? = null,
            checkForConflict: ((String) -> Boolean)? = null
        ): String {
            val uuid = UUID.randomUUID().toString().lowercase()
            val progressedUuid = processString?.invoke(uuid) ?: uuid
            return if (checkForConflict?.invoke(progressedUuid) == true) {
                generateUniqueUUID(processString, checkForConflict)
            } else {
                progressedUuid
            }
        }
    }
}
