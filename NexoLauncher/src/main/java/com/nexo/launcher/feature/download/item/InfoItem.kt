package com.nexo.launcher.feature.download.item

import com.nexo.launcher.feature.download.enums.Category
import com.nexo.launcher.feature.download.enums.Classify
import com.nexo.launcher.feature.download.enums.Platform
import java.util.Date

/**
 * åŸºç¡€çš„ä¿¡æ¯ç±»
 * @param classify è¯¥é¡¹ç›®çš„ç±»åˆ«
 * @param platform è¯¥é¡¹ç›®çš„æ‰€å±žå¹³å°
 * @param projectId è¯¥é¡¹ç›®çš„å”¯ä¸€æ ‡è¯†
 * @param slug è¯¥é¡¹ç›®çš„slug
 * @param author è¯¥é¡¹ç›®çš„ä½œè€…
 * @param title è¯¥é¡¹ç›®çš„æ ‡é¢˜
 * @param description è¯¥é¡¹ç›®çš„æè¿°
 * @param downloadCount è¯¥é¡¹ç›®çš„æ€»ä¸‹è½½é‡
 * @param uploadDate è¯¥é¡¹ç›®çš„ä¸Šä¼ æ—¥æœŸ
 * @param iconUrl è¯¥é¡¹ç›®çš„å°é¢é“¾æŽ¥
 * @param category è¯¥é¡¹ç›®çš„æ ‡ç­¾
 */
open class InfoItem(
    val classify: Classify,
    val platform: Platform,
    val projectId: String,
    val slug: String,
    val author: Array<String>?,
    val title: String,
    val description: String,
    val downloadCount: Long,
    val uploadDate: Date,
    val iconUrl: String?,
    val category: List<Category>
) {
    fun copy() = InfoItem(
        classify, platform, projectId, slug, author, title, description, downloadCount, uploadDate, iconUrl, category
    )

    override fun toString(): String {
        return "InfoItem(" +
                "classify='$classify', " +
                "platform='$platform', " +
                "projectId='$projectId', " +
                "slug='$slug', " +
                "author=${author.contentToString()}, " +
                "title='$title', " +
                "description='$description', " +
                "downloadCount=$downloadCount, " +
                "uploadDate=$uploadDate, " +
                "iconUrl='$iconUrl', " +
                "category=$category" +
                ")"
    }
}
