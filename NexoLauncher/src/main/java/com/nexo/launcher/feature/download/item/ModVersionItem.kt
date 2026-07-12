package com.nexo.launcher.feature.download.item

import com.nexo.launcher.feature.download.enums.ModLoader
import com.nexo.launcher.feature.download.enums.VersionType
import java.util.Date

/**
 * @param dependencies è¯¥ç‰ˆæœ¬çš„ä¾èµ– Mod çš„ä¿¡æ¯
 */
class ModVersionItem(
    projectId: String,
    title: String,
    downloadCount: Long,
    uploadDate: Date,
    mcVersions: List<String>,
    versionType: VersionType,
    fileName: String,
    fileHash: String?,
    fileUrl: String,
    modloaders: List<ModLoader>,
    val dependencies: List<DependenciesInfoItem>
) : ModLikeVersionItem(
    projectId, title, downloadCount, uploadDate, mcVersions, versionType, fileName, fileHash, fileUrl, modloaders
) {
    override fun toString(): String {
        return "ModVersionItem(" +
                "projectId='$projectId', " +
                "title='$title', " +
                "downloadCount=$downloadCount, " +
                "uploadDate=$uploadDate, " +
                "mcVersions=$mcVersions, " +
                "versionType=$versionType, " +
                "fileName=$fileName" +
                "fileHash='$fileHash', " +
                "fileUrl='$fileUrl', " +
                "modloaders=$modloaders" +
                "dependencies=$dependencies" +
                ")"
    }
}
