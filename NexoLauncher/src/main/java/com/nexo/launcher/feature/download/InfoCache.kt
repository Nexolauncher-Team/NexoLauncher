package com.nexo.launcher.feature.download

import com.nexo.launcher.feature.download.item.DependenciesInfoItem
import com.nexo.launcher.feature.download.item.ModLikeVersionItem
import com.nexo.launcher.feature.download.item.ModVersionItem
import com.nexo.launcher.feature.download.item.VersionItem

/**
 * å°†æœç´¢å¾—åˆ°çš„ä¿¡æ¯ç¼“å­˜åœ¨å†…å­˜ä¸­ï¼Œä¸‹æ¬¡åŠ è½½æ—¶å¯ç›´æŽ¥ä»Žå†…å­˜ä¸­æ‹¿åˆ°ä¸Šæ¬¡çš„æœç´¢ç»“æžœ
 */
class InfoCache {
    abstract class CacheBase<V> {
        private val cache: MutableMap<String, V> = HashMap()

        /**
         * æ ¹æ®ModIdï¼Œå°†æœç´¢åˆ°çš„å€¼å­˜å…¥å†…å­˜
         */
        fun put(modId: String, value: V) {
            cache[modId] = value
        }

        /**
         * æ ¹æ®ModIdï¼Œæ‹¿åˆ°å†…å­˜ä¸­å­˜å‚¨çš„å€¼ï¼Œè‹¥æ²¡æœ‰ï¼Œåˆ™è¿”å›žç©º
         */
        fun get(modId: String): V? {
            return cache[modId]
        }

        /**
         * æ£€æŸ¥å†…å­˜ä¸­æ˜¯å¦å­˜åœ¨å·²ç»å­˜å…¥çš„ModId
         */
        fun containsKey(modId: String): Boolean {
            return cache.containsKey(modId)
        }
    }

    object DependencyInfoCache : CacheBase<DependenciesInfoItem>()
    object VersionCache : CacheBase<MutableList<VersionItem>>()
    object ModVersionCache : CacheBase<MutableList<ModVersionItem>>()
    object ModPackVersionCache : CacheBase<MutableList<ModLikeVersionItem>>()
}
