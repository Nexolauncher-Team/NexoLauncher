package com.nexo.launcher.feature.download

import com.nexo.launcher.feature.download.enums.Category
import com.nexo.launcher.feature.download.enums.ModLoader
import com.nexo.launcher.feature.download.enums.Sort

/**
 * ç”¨äºŽå¹³å°è¿›è¡Œæœç´¢æ—¶ï¼Œæä¾›ç­›é€‰ä¿¡æ¯
 */
class Filters {
    var name: String = ""
    var mcVersion: String? = null
    var modloader: ModLoader? = null
    var sort: Sort = Sort.RELEVANT
    var category: Category = Category.ALL

    override fun toString(): String {
        return "Filters(name='$name', mcVersion=$mcVersion, modloader=$modloader, sort=$sort, category=$category)"
    }
}
