package com.nexo.launcher.event.value

/**
 * ä¸‹è½½é¡µé¢çš„ä¸€äº›äº‹ä»¶
 */
class DownloadPageEvent {
    /**
     * åˆ‡æ¢ä¸‹è½½é¡µé¢æ—¶ï¼Œä½¿ç”¨è¿™ä¸ªäº‹ä»¶é€šçŸ¥Fragmentæ’­æ”¾åŠ¨ç”»
     * @param index Fragmentçš„ç±»åˆ«ç´¢å¼•
     * @param classify åŠ¨ç”»ç±»åž‹ï¼ˆINï¼šè¿›å…¥åŠ¨ç”»ï¼ŒOUTï¼šé€€å‡ºåŠ¨ç”»ï¼‰
     */
    class PageSwapEvent(val index: Int, val classify: Int) {
        companion object {
            const val IN = 0
            const val OUT = 1
        }
    }

    /**
     * ä¸‹è½½é¡µé¢å·²é”€æ¯äº‹ä»¶
     */
    class PageDestroyEvent

    /**
     * æ˜¯å¦ç¦ç”¨RecyclerView
     */
    class RecyclerEnableEvent(val enable: Boolean)
}
