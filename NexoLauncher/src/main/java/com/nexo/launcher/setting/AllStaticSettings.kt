package com.nexo.launcher.setting

/**
 * é™æ€è®¾ç½®é¡¹çš„å€¼ï¼Œç”¨äºŽä¸€äº›ä¸´æ—¶ç”Ÿæ•ˆçš„è®¾ç½®é¡¹ä½¿ç”¨
 * è¿™é‡Œçš„å€¼ä¸ä¼šè¢«ä¿å­˜åˆ°è®¾ç½®é…ç½®ä¸­ï¼Œè½¯ä»¶é‡å¯å°±ä¼šæ¶ˆå¤±ï¼
 */
class AllStaticSettings {
    companion object {
        /**
         * åˆ˜æµ·å±ç¼ºå£å®½åº¦ Int
         */
        @JvmField var notchSize = 0

        /**
         * ç¼©æ”¾å› å­ Float
         */
        @JvmField var scaleFactor = AllSettings.resolutionRatio.getValue() / 100f

        /**
         * ç¦ç”¨åŒå‡»äº¤æ¢æ‰‹ä¸­ç‰©å“ Boolean
         */
        @JvmField var disableDoubleTap = AllSettings.disableDoubleTap.getValue()

        /**
         * è§¦å‘é•¿æŒ‰å»¶è¿Ÿ Int
         */
        @JvmField var timeLongPressTrigger = AllSettings.timeLongPressTrigger.getValue()

        /**
         * å¯ç”¨é™€èžºä»ªæŽ§åˆ¶ Boolean
         */
        @JvmField var enableGyro = AllSettings.enableGyro.getValue()

        /**
         * é™€èžºä»ªæŽ§åˆ¶çµæ•åº¦ Int
         */
        @JvmField var gyroSensitivity = AllSettings.gyroSensitivity.getValue()

        /**
         * é™€èžºä»ªåè½¬Xè½´ Boolean
         */
        @JvmField var gyroInvertX = AllSettings.gyroInvertX.getValue()

        /**
         * é™€èžºä»ªåè½¬Yè½´ Boolean
         */
        @JvmField var gyroInvertY = AllSettings.gyroInvertY.getValue()

        /**
         * ä½¿ç”¨æŽ§åˆ¶ä»£ç† Boolean
         */
        @JvmField var useControllerProxy = false
    }
}
