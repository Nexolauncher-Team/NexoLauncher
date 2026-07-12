package com.nexo.launcher.feature.download.item

import com.nexo.launcher.feature.download.enums.VersionType
import java.util.Date

/**
 * ç‰ˆæœ¬ä¿¡æ¯ç±»
 * @param projectId è¯¥ç‰ˆæœ¬æ‰€åœ¨é¡¹ç›®çš„å”¯ä¸€æ ‡è¯†
 * @param title è¯¥ç‰ˆæœ¬çš„æ ‡é¢˜
 * @param downloadCount è¯¥ç‰ˆæœ¬çš„æ€»ä¸‹è½½é‡
 * @param uploadDate è¯¥ç‰ˆæœ¬çš„ä¸Šä¼ æ—¥æœŸ
 * @param mcVersions è¯¥ç‰ˆæœ¬çš„ MCç‰ˆæœ¬
 * @param versionType è¯¥ç‰ˆæœ¬çš„ç‰ˆæœ¬çŠ¶æ€
 * @param fileName è¯¥ç‰ˆæœ¬çš„æ–‡ä»¶åç§°
 * @param fileHash è¯¥ç‰ˆæœ¬çš„æ–‡ä»¶HASHå€¼
 * @param fileUrl è¯¥ç‰ˆæœ¬çš„æ–‡ä»¶ä¸‹è½½é“¾æŽ¥
 */
open class VersionItem(
    val projectId: String,
    val title: String,
    val downloadCount: Long,
    val uploadDate: Date,
    val mcVersions: List<String>,
    val versionType: VersionType,
    val fileName: String,
    val fileHash: String?,
    val fileUrl: String
) {
    override fun toString(): String {
        return "VersionItem(" +
                "projectId='$projectId', " +
                "title='$title', " +
                "downloadCount=$downloadCount, " +
                "uploadDate=$uploadDate, " +
                "mcVersions=$mcVersions, " +
                "versionType=$versionType, " +
                "fileName='$fileName'" +
                "fileHash='$fileHash'" +
                "fileUrl='$fileUrl'" +
                ")"
    }
}
