package com.nexo.launcher.feature.version.utils

import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.nexo.launcher.R
import com.nexo.launcher.feature.version.Version
import com.nexo.launcher.feature.version.VersionsManager
import org.apache.commons.io.FileUtils

/**
 * ç”¨äºŽè‡ªåŠ¨è®¾ç½®ç‰ˆæœ¬çš„å›¾æ ‡ï¼Œæˆ–è€…é‡ç½®å¯¹åº”ç‰ˆæœ¬çš„è‡ªå®šä¹‰å›¾æ ‡
 */
class VersionIconUtils(
    private val version: Version
) {
    private val iconFile = VersionsManager.getVersionIconFile(version)

    /**
     * é€šè¿‡ç‰ˆæœ¬æ¥è¯†åˆ«å…¶é»˜è®¤çš„å›¾æ ‡ï¼Œæ¯”å¦‚åŽŸç‰ˆã€æ¨¡ç»„åŠ è½½å™¨å°é¢å›¾ï¼Œå¦‚æžœæœ‰è‡ªå®šä¹‰å›¾æ ‡ï¼Œé‚£ä¹ˆä¼šä¼˜å…ˆè®¾ç½®è‡ªå®šä¹‰å›¾æ ‡
     * @return è¿”å›žæ˜¯å¦è®¾ç½®ä¸ºäº†è‡ªå®šä¹‰å›¾æ ‡ï¼Œä¾¿äºŽä½¿ç”¨é‡ç½®å›¾æ ‡çš„æ“ä½œ
     */
    fun start(imageView: ImageView): Boolean {
        val context = imageView.context

        var isIconSet = false
        var isCustomIcon = false

        iconFile.let { icon ->
            if (icon.exists()) {
                Glide.with(imageView)
                    .load(icon)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(imageView)
                isIconSet = true
                isCustomIcon = true
            }
        }

        version.getVersionInfo()?.let { versionInfo ->
            versionInfo.loaderInfo?.forEach { loaderInfo ->
                if (!isIconSet) {
                    getLoaderIcon(loaderInfo.name)?.let { icon ->
                        imageView.setImageDrawable(ContextCompat.getDrawable(context, icon))
                        isIconSet = true
                    }
                } else return@forEach
            }
        }

        if (!isIconSet) imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_minecraft))

        return isCustomIcon
    }

    /**
     * é€šè¿‡åˆ é™¤è‡ªå®šä¹‰å›¾æ ‡æ–‡ä»¶ï¼Œæ¥è¾¾æˆé‡ç½®çš„ç›®çš„
     * **è¿™ä¸ªæ“ä½œä¸å¯é€†**
     */
    fun resetIcon() {
        FileUtils.deleteQuietly(iconFile)
    }

    /**
     * @return èŽ·å–å½“å‰ç‰ˆæœ¬çš„å°é¢å›¾æ ‡
     */
    fun getIconFile() = iconFile

    private fun getLoaderIcon(name: String): Int? {
        return when(name.lowercase()) {
            "fabric" -> R.drawable.ic_fabric
            "forge" -> R.drawable.ic_anvil
            "quilt" -> R.drawable.ic_quilt
            "neoforge" -> R.drawable.ic_neoforge
            "optifine" -> R.drawable.ic_optifine
            "liteloader" -> R.drawable.ic_chicken_old
            else -> null
        }
    }
}
