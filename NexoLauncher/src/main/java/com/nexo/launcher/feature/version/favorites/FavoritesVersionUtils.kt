package com.nexo.launcher.feature.version.favorites

import com.nexo.launcher.feature.version.VersionsManager
import java.util.concurrent.ConcurrentHashMap

class FavoritesVersionUtils private constructor() {
    companion object {
        private inline fun modifyFavorites(action: (MutableMap<String, MutableSet<String>>) -> Unit) {
            VersionsManager.currentGameInfo.apply {
                action(favoritesMap)
                saveCurrentInfo()
            }
        }

        /**
         * åŽŸå­åŒ–é‡å‘½åç‰ˆæœ¬
         */
        fun renameVersion(oldName: String, newName: String) = modifyFavorites { map ->
            map.values.forEach { versions ->
                if (oldName in versions) {
                    versions.remove(oldName)
                    versions.add(newName)
                }
            }
        }

        /**
         * æ·»åŠ ä¸€ä¸ªæ”¶è—å¤¹
         */
        fun addFolder(name: String) = modifyFavorites { map ->
            map.putIfAbsent(name, ConcurrentHashMap.newKeySet())
        }

        /**
         * ç§»é™¤ä¸€ä¸ªæ”¶è—å¤¹
         */
        fun removeFolder(name: String) = modifyFavorites { map ->
            map.remove(name)
        }

        /**
         * æ›´æ–°ç‰ˆæœ¬æ”¶è—å¤¹
         * @param version ç›®æ ‡ç‰ˆæœ¬
         * @param targetFolders éœ€è¦åŒ…å«è¯¥ç‰ˆæœ¬çš„æ”¶è—å¤¹é›†åˆ
         */
        fun updateVersionFolders(version: String, targetFolders: Set<String>) = modifyFavorites { map ->
            //æ·»åŠ è‡³ç›®æ ‡æ”¶è—å¤¹
            targetFolders.forEach { folder ->
                map.getOrPut(folder) { ConcurrentHashMap.newKeySet() }.add(version)
            }

            //ä»Žéžç›®æ ‡æ”¶è—å¤¹ç§»é™¤
            map.keys.filterNot { it in targetFolders }.forEach { folder ->
                map[folder]?.remove(version)
            }
        }

        /**
         * èŽ·å–æœ‰æ•ˆæ”¶è—å¤¹ç»“æž„
         */
        fun getFavoritesStructure(): Map<String, Set<String>> =
            VersionsManager.currentGameInfo.favoritesMap.let { map ->
                map.entries.associate { (k, v) -> k to v.toSet() }
            }

        /**
         * èŽ·å–æŒ‡å®šæ”¶è—å¤¹çš„æœ‰æ•ˆç‰ˆæœ¬
         */
        fun getValidVersions(folder: String): Set<String> =
            VersionsManager.currentGameInfo.favoritesMap[folder]
                ?.filter { VersionsManager.checkVersionExistsByName(it) }
                .orEmpty()
                .toSet()
    }
}
