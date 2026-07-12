package com.nexo.launcher.event.value

/**
 * å½“æœ‰æ–°çš„ä¸‹è½½ä»»åŠ¡æ—¶ï¼Œä½¿ç”¨è¿™ä¸ªä»»åŠ¡å‘LauncherActivityé€šçŸ¥ä»»åŠ¡çš„é”®
 * æ–¹ä¾¿ç›‘å¬è¿™ä¸ªä»»åŠ¡çš„ä¸‹è½½è¿›åº¦
 * @param observe æ˜¯å¦ç»§ç»­ç›‘å¬
 * @see com.nexo.launcher.LauncherActivity
 */
class DownloadProgressKeyEvent(val progressKey: String, val observe: Boolean)
