package com.nexo.launcher.utils

import com.nexo.launcher.R
import com.nexo.launcher.context.ContextExecutor.Companion.getString
import com.nexo.launcher.utils.stringutils.StringUtils
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

class NumberWithUnits {
    companion object {
        private val UNITS_EN = arrayOf("", "K", "M") //è‹±æ–‡å•ä½ï¼šåƒã€ç™¾ä¸‡
        private val UNITS_ZH = arrayOf(
            "",
            getString(R.string.generic_wan),
            getString(R.string.generic_yi)
        ) //ä¸­æ–‡å•ä½:ä¸‡ã€äº¿

        @JvmStatic
        fun formatNumberWithUnit(number: Long, isEnglish: Boolean): String {
            return if (isEnglish) {
                formatNumberWithUnitEnglish(number)
            } else {
                formatNumberWithUnitChinese(number)
            }
        }

        private fun formatNumberWithUnitChinese(number: Long): String {
            return formatNumber(number, 10000, UNITS_ZH)
        }

        private fun formatNumberWithUnitEnglish(number: Long): String {
            return formatNumber(number, 1000, UNITS_EN)
        }

        private fun formatNumber(number: Long, stage: Int, units: Array<String>): String {
            var bigDecimal = BigDecimal(number)
            var unitIndex = 0

            while (bigDecimal >= BigDecimal.valueOf(stage.toLong()) && unitIndex < units.size - 1) {
                bigDecimal = bigDecimal.divide(BigDecimal.valueOf(stage.toLong()), 2, RoundingMode.DOWN)
                unitIndex++
            }

            //æ£€æŸ¥æ˜¯å¦ä¸ºç©ºçš„å•ä½ï¼Œå¦‚æžœæ˜¯ï¼Œé‚£ä¹ˆå°±ä¸åšæ ¼å¼åŒ–ï¼Œç›´æŽ¥è¿”å›žåŽŸå§‹å€¼
            if (units[unitIndex].isEmpty()) {
                return number.toString()
            } else {
                val df = DecimalFormat("#.00")
                val formattedNumber = df.format(bigDecimal.setScale(2, RoundingMode.DOWN).toDouble())
                return StringUtils.insertSpace(formattedNumber, units[unitIndex])
            }
        }
    }
}

