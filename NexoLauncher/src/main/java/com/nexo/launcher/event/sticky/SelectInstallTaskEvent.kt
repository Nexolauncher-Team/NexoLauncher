package com.nexo.launcher.event.sticky

import com.nexo.launcher.feature.version.install.Addon
import com.nexo.launcher.feature.version.install.InstallTask

/**
 * é€‰æ‹©å®‰è£…ä»»åŠ¡åŽï¼Œå°†ä½¿ç”¨è¿™ä¸ªäº‹ä»¶è¿›è¡Œé€šçŸ¥
 * @param addon é€‰æ‹©çš„æ˜¯è°çš„å®‰è£…ä»»åŠ¡
 * @param selectedVersion é€‰æ‹©çš„ç‰ˆæœ¬
 * @param task é€‰æ‹©çš„ä»»åŠ¡
 * @see com.nexo.launcher.feature.version.install.Addon
 */
class SelectInstallTaskEvent(val addon: Addon, val selectedVersion: String, val task: InstallTask)
