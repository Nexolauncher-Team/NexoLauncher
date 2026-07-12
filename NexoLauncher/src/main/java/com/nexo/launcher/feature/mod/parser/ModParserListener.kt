package com.nexo.launcher.feature.mod.parser

/**
 * æ¨¡ç»„è§£æžè¿›åº¦ç›‘å¬å™¨ï¼Œç”¨äºŽå›žè°ƒå½“å‰å·²ç»å¤„ç†çš„æ¨¡ç»„å’Œæ¨¡ç»„æ€»æ•°
 */
interface ModParserListener {
    /**
     * è§£æžè¿›åº¦å›žè°ƒï¼Œé€šè¿‡è¿™ä¸ªå‡½æ•°å›žè°ƒå½“å‰æ¨¡ç»„çš„è§£æžè¿›åº¦
     * @param recentlyParsedModInfo åˆšåˆšè§£æžå®Œæˆçš„æ¨¡ç»„ä¿¡æ¯
     * @param totalFileCount æ‰€æœ‰éœ€è¦æ£€æŸ¥çš„æ–‡ä»¶çš„æ•°é‡
     */
    fun onProgress(recentlyParsedModInfo: ModInfo, totalFileCount: Int)

    /**
     * è§£æžå®ŒæˆåŽé€šè¿‡è¿™ä¸ªå‡½æ•°å°†è§£æžçš„ç»“æžœè¿›è¡Œå›žè°ƒ
     * @param modInfoList æ‰€æœ‰æ¨¡ç»„ä¿¡æ¯åˆ—è¡¨
     */
    fun onParseEnded(modInfoList: List<ModInfo>)
}
