package com.nexo.launcher.ui.subassembly.hotbar

import com.nexo.launcher.R

/**
 * å¿«æ·æ åˆ¤å®šç±»åž‹
 * @param nameId ç±»åž‹çš„æœ¬åœ°åŒ–åç§°id
 * @param valueName ç±»åž‹çš„è®¾ç½®å­˜å‚¨å€¼
 */
enum class HotbarType(val nameId: Int, val valueName: String) {
    /**
     * è‡ªé€‚åº”ï¼šæ ¹æ®å±å¹•åˆ†è¾¨çŽ‡ã€GUIç¼©æ”¾å°ºå¯¸ï¼Œä¸ºåˆ¤å®šæ¡†è‡ªåŠ¨è®¡ç®—å‡ºåˆé€‚çš„å®½ä¸Žé«˜ï¼ˆå¯èƒ½ä¼šä¸ç²¾å‡†ï¼‰
     */
    AUTO(R.string.option_hotbar_type_auto, "auto"),

    /**
     * æ‰‹åŠ¨ï¼šè®©ç”¨æˆ·è‡ªè¡Œè°ƒæ•´åˆ¤å®šæ¡†çš„å®½ä¸Žé«˜
     */
    MANUALLY(R.string.option_hotbar_type_manually, "manually")
}
