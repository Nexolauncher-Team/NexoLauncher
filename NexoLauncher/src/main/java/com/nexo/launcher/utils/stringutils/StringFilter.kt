package com.nexo.launcher.utils.stringutils

import java.util.Locale
import java.util.regex.Pattern

class StringFilter {
    companion object {
        /**
         * æ£€æŸ¥è¾“å…¥å­—ç¬¦ä¸²æ˜¯å¦åŒ…å«æŒ‡å®šçš„å­å­—ç¬¦ä¸²ã€‚
         * @param input è¾“å…¥å­—ç¬¦ä¸²
         * @param substring æ£€æŸ¥å­å­—ç¬¦ä¸²
         * @param caseSensitive æ˜¯å¦åŒºåˆ†å¤§å°å†™
         * @return å¦‚æžœè¾“å…¥å­—ç¬¦ä¸²åŒ…å«æŒ‡å®šçš„å­å­—ç¬¦ä¸²ï¼Œè¿”å›žtrueï¼›å¦åˆ™è¿”å›žfalse
         */
        @JvmStatic
        fun containsSubstring(input: String, substring: String, caseSensitive: Boolean): Boolean {
            val adjustedInput = if (caseSensitive) input else input.lowercase(Locale.getDefault())
            val adjustedSubstring =
                if (caseSensitive) substring else substring.lowercase(Locale.getDefault())
            val regex = Pattern.quote(adjustedSubstring)
            val compiledPattern = Pattern.compile(regex)
            val matcher = compiledPattern.matcher(adjustedInput)
            return matcher.find()
        }
    }
}

