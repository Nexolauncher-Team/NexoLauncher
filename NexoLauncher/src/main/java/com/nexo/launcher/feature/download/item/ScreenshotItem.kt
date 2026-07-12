package com.nexo.launcher.feature.download.item

/**
 * å±å¹•æˆªå›¾ä¿¡æ¯è®°å½•
 * @param imageUrl æˆªå›¾çš„åœ°å€
 * @param title æˆªå›¾çš„æ ‡é¢˜
 * @param description æˆªå›¾çš„æè¿°
 */
class ScreenshotItem(
    val imageUrl: String,
    val title: String?,
    val description: String?
) {
    override fun toString(): String {
        return "ScreenshotItem(imageUrl='$imageUrl', title='$title', description='$description')"
    }
}
