package com.nexo.launcher.event.single

/**
 * å½“ç‰ˆæœ¬åˆ—è¡¨åˆ·æ–°æ—¶ï¼Œé€šè¿‡æ­¤äº‹ä»¶è¿›è¡Œé€šçŸ¥
 * åˆ·æ–°ç‰ˆæœ¬æ˜¯å¼‚æ­¥è¿›è¡Œçš„ï¼Œæ‰€ä»¥éœ€è¦ç¡®ä¿æŽ¥æ”¶äº‹ä»¶æ—¶ï¼Œåœ¨UIçº¿ç¨‹è¿è¡Œ
 * @see com.nexo.launcher.feature.version.VersionsManager
 */
class RefreshVersionsEvent(val mode: MODE) {
    enum class MODE {
        START, END
    }
}
