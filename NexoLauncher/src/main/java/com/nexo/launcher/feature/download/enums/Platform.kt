package com.nexo.launcher.feature.download.enums

import com.nexo.launcher.feature.download.platform.AbstractPlatformHelper
import com.nexo.launcher.feature.download.platform.curseforge.CurseForgeHelper
import com.nexo.launcher.feature.download.platform.modrinth.ModrinthHelper

enum class Platform(val pName: String, val helper: AbstractPlatformHelper) {
    MODRINTH("Modrinth", ModrinthHelper()),
    CURSEFORGE("CurseForge", CurseForgeHelper())
}
