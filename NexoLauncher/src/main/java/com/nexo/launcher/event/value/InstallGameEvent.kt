package com.nexo.launcher.event.value

import com.nexo.launcher.feature.version.install.Addon
import com.nexo.launcher.feature.version.install.InstallTaskItem

/**
 * å®‰è£…ä»»åŠ¡å¼€å§‹æ—¶ï¼Œå°†ä½¿ç”¨è¿™ä¸ªäº‹ä»¶è¿›è¡Œé€šçŸ¥
 * @see com.nexo.launcher.ui.fragment.InstallGameFragment
 * @param minecraftVersion MCåŽŸç‰ˆç‰ˆæœ¬
 * @param customVersionName è‡ªå®šä¹‰çš„ç‰ˆæœ¬æ–‡ä»¶å¤¹åç§°
 * @param taskMap å®‰è£…ä»»åŠ¡
 */
class InstallGameEvent(
    val minecraftVersion: String,
    val customVersionName: String,
    val taskMap: Map<Addon, InstallTaskItem>
)
