package com.nexo.launcher.feature.mod.modpack.install

import android.app.Activity
import com.nexo.launcher.R
import com.nexo.launcher.utils.LauncherProfiles
import com.nexo.launcher.feature.download.item.ModLoaderWrapper
import com.nexo.launcher.feature.log.Logging
import com.nexo.launcher.feature.mod.models.MCBBSPackMeta
import com.nexo.launcher.utils.runtime.SelectRuntimeUtils
import com.nexo.launcher.JavaGUILauncherActivity
import com.nexo.launcher.Tools
import com.nexo.launcher.modloaders.modpacks.models.CurseManifest
import com.nexo.launcher.modloaders.modpacks.models.ModrinthIndex
import java.io.File
import java.util.zip.ZipFile

class ModPackUtils {
    companion object {
        @JvmStatic
        fun determineModpack(modpack: File): ModPackInfo {
            val zipName = modpack.name
            val suffix = zipName.substring(zipName.lastIndexOf('.'))
            runCatching {
                ZipFile(modpack).use { modpackZipFile ->
                    if (suffix == ".zip") {
                        val mcbbsEntry = modpackZipFile.getEntry("mcbbs.packmeta")
                        val curseforgeEntry = modpackZipFile.getEntry("manifest.json")
                        if (mcbbsEntry == null && curseforgeEntry != null) {
                            val curseManifest = Tools.GLOBAL_GSON.fromJson(
                                Tools.read(modpackZipFile.getInputStream(curseforgeEntry)),
                                CurseManifest::class.java
                            )
                            if (verifyManifest(curseManifest)) return ModPackInfo(curseManifest.name, ModPackEnum.CURSEFORGE)
                        } else if (mcbbsEntry != null) {
                            val mcbbsPackMeta = Tools.GLOBAL_GSON.fromJson(
                                Tools.read(modpackZipFile.getInputStream(mcbbsEntry)),
                                MCBBSPackMeta::class.java
                            )
                            if (verifyMCBBSPackMeta(mcbbsPackMeta)) return ModPackInfo(mcbbsPackMeta.name, ModPackEnum.MCBBS)
                        }
                    } else if (suffix == ".mrpack") {
                        val entry = modpackZipFile.getEntry("modrinth.index.json")
                        if (entry != null) {
                            val modrinthIndex = Tools.GLOBAL_GSON.fromJson(
                                Tools.read(modpackZipFile.getInputStream(entry)),
                                ModrinthIndex::class.java
                            )
                            if (verifyModrinthIndex(modrinthIndex)) return ModPackInfo(modrinthIndex.name, ModPackEnum.MODRINTH)
                        }
                    }
                }
            }.onFailure { e ->
                Logging.e("determineModpack", "There was a problem checking the ModPack", e)
            }

            return ModPackInfo(null, ModPackEnum.UNKNOWN)
        }

        @JvmStatic
        fun verifyManifest(manifest: CurseManifest): Boolean { //æ£€æµ‹æ˜¯å¦ä¸ºcurseforgeæ•´åˆåŒ…(é€šè¿‡manifest.jsonå†…çš„æ•°æ®è¿›è¡Œåˆ¤æ–­)
            if ("minecraftModpack" != manifest.manifestType) return false
            if (manifest.manifestVersion != 1) return false
            if (manifest.minecraft == null) return false
            if (manifest.minecraft.version == null) return false
            if (manifest.minecraft.modLoaders == null) return false
            return manifest.minecraft.modLoaders.isNotEmpty()
        }

        @JvmStatic
        fun verifyModrinthIndex(modrinthIndex: ModrinthIndex): Boolean { //æ£€æµ‹æ˜¯å¦ä¸ºmodrinthæ•´åˆåŒ…(é€šè¿‡modrinth.index.jsonå†…çš„æ•°æ®è¿›è¡Œåˆ¤æ–­)
            if ("minecraft" != modrinthIndex.game) return false
            if (modrinthIndex.formatVersion != 1) return false
            return modrinthIndex.dependencies != null
        }

        fun verifyMCBBSPackMeta(mcbbsPackMeta: MCBBSPackMeta): Boolean { //æ£€æµ‹æ˜¯å¦ä¸ºMCBBSæ•´åˆåŒ…(é€šè¿‡mcbbs.packmetaå†…çš„æ•°æ®è¿›è¡Œåˆ¤æ–­)
            if ("minecraftModpack" != mcbbsPackMeta.manifestType) return false
            if (mcbbsPackMeta.manifestVersion != 2) return false
            if (mcbbsPackMeta.addons == null) return false
            if (mcbbsPackMeta.addons[0].id == null) return false
            return (mcbbsPackMeta.addons[0].version != null)
        }

        @JvmStatic
        @Throws(Throwable::class)
        fun startModLoaderInstall(modLoader: ModLoaderWrapper, activity: Activity, modInstallFile: File, customName: String) {
            modLoader.getInstallationIntent(activity, modInstallFile, customName)?.let { installIntent ->
                SelectRuntimeUtils.selectRuntime(activity, activity.getString(R.string.version_install_new_modloader, modLoader.modLoader.loaderName)) { jreName ->
                    LauncherProfiles.generateLauncherProfiles()
                    installIntent.putExtra(JavaGUILauncherActivity.EXTRAS_JRE_NAME, jreName)
                    activity.startActivity(installIntent)
                }
            }
        }
    }

    enum class ModPackEnum {
        UNKNOWN, CURSEFORGE, MCBBS, MODRINTH
    }
}

