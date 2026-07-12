package com.nexo.launcher.event.value

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

/**
 * å°†ä¸€ä¸ªæ–°çš„Fragmentæ·»åŠ åˆ°äº‹åŠ¡ç®¡ç†ä¸­ï¼Œç”±LauncherActivityæŽ¥å—å¹¶å¤„ç†
 * ä¿è¯Fragmentæ·»åŠ çš„æ—¶å€™ï¼Œçˆ¶Fragmentä¸€å®šæ˜¯å½“å‰çš„Fragment
 * @see com.nexo.launcher.LauncherActivity
 * @see com.nexo.launcher.utils.ZHTools.addFragment
 */
class AddFragmentEvent(
    val fragmentClass: Class<out Fragment?>,
    val fragmentTag: String?,
    val bundle: Bundle?,
    val fragmentActivityCallback: FragmentActivityCallBack?
) {
    /**
     * å¯¹äºŽå½“å‰Fragmentçš„FragmentActivityçš„ä¸€äº›å›žè°ƒå¤„ç†
     */
    fun interface FragmentActivityCallBack {
        fun callBack(fragmentActivity: FragmentActivity)
    }
}
