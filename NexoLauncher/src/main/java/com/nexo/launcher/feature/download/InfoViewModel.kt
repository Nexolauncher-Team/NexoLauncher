package com.nexo.launcher.feature.download

import androidx.lifecycle.ViewModel
import com.nexo.launcher.feature.download.item.InfoItem
import com.nexo.launcher.feature.download.platform.AbstractPlatformHelper

class InfoViewModel : ViewModel() {
    var platformHelper: AbstractPlatformHelper? = null
    var infoItem: InfoItem? = null
}
