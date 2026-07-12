package com.nexo.launcher.feature.download.item

/**
 * ç”¨äºŽè®°å½•æœç´¢ç»“æžœ
 */
class SearchResult {
    var previousCount: Int = 0
    var totalResultCount: Int = 0
    val infoItems: MutableList<InfoItem> = ArrayList()
    var isLastPage: Boolean = false

    override fun toString(): String {
        return "SearchResult(previousCount=$previousCount, totalResultCount=$totalResultCount, infoItems=$infoItems, isLastPage=$isLastPage)"
    }
}
