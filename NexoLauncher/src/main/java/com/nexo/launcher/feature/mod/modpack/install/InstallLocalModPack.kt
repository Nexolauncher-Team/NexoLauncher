package com.nexo.launcher.feature.mod.modpack.install

import android.content.Context
import com.nexo.launcher.R
import com.nexo.launcher.feature.download.item.ModLoaderWrapper
import com.nexo.launcher.feature.download.platform.curseforge.CurseForgeModPackInstallHelper
import com.nexo.launcher.feature.download.platform.modrinth.ModrinthModPackInstallHelper
import com.nexo.launcher.feature.download.utils.PlatformUtils
import com.nexo.launcher.feature.log.Logging
import com.nexo.launcher.feature.mod.models.MCBBSPackMeta
import com.nexo.launcher.feature.mod.modpack.MCBBSModPack
import com.nexo.launcher.feature.mod.modpack.install.ModPackUtils.ModPackEnum
import com.nexo.launcher.feature.version.VersionConfig
import com.nexo.launcher.feature.version.VersionsManager
import com.nexo.launcher.task.TaskExecutors
import com.nexo.launcher.ui.dialog.TipDialog
import com.nexo.launcher.utils.stringutils.StringUtils
import com.nexo.launcher.Tools
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.zip.ZipFile

class InstallLocalModPack {
    companion object {
        @JvmStatic
        @Throws(Exception::class)
        fun installModPack(
            context: Context,
            type: ModPackEnum?,
            zipFile: File,
            customVersionName: String
        ): ModLoaderWrapper? {
            try {
                runCatching {
                    ZipFile(zipFile)
                }.getOrElse {
                    Logging.e("Install local ModPack", "This file doesn't seem to be a proper archive", it)
                    TaskExecutors.runInUIThread {
                        showUnSupportDialog(context)
                    }
                    return null
                }.use { modpackZipFile ->
                    val modLoader: ModLoaderWrapper?
                    val versionPath = VersionsManager.getVersionPath(customVersionName)

                    when (type) {
                        ModPackEnum.CURSEFORGE -> {
                            modLoader = curseforgeModPack(zipFile, versionPath) ?: return null
                            VersionConfig.createIsolation(versionPath).save()

                            return modLoader
                        }

                        ModPackEnum.MCBBS -> {
                            val mcbbsEntry = modpackZipFile.getEntry("mcbbs.packmeta")

                            val mcbbsPackMeta = Tools.GLOBAL_GSON.fromJson(
                                Tools.read(
                                    modpackZipFile.getInputStream(mcbbsEntry)
                                ), MCBBSPackMeta::class.java
                            )

                            modLoader = mcbbsModPack(context, zipFile, versionPath) ?: return null
                            VersionConfig.createIsolation(versionPath).apply {
                                setJavaArgs(StringUtils.insertSpace(null, *mcbbsPackMeta.launchInfo.javaArgument))
                            }.save()

                            return modLoader
                        }

                        ModPackEnum.MODRINTH -> {
                            modLoader = modrinthModPack(zipFile, versionPath) ?: return null
                            VersionConfig.createIsolation(versionPath).save()

                            return modLoader
                        }

                        else -> {
                            TaskExecutors.runInUIThread {
                                showUnSupportDialog(context)
                            }
                            return null
                        }
                    }
                }
            } finally {
                FileUtils.deleteQuietly(zipFile) // åˆ é™¤æ–‡ä»¶ï¼ˆè™½ç„¶æ–‡ä»¶é€šå¸¸æ¥è¯´å¹¶ä¸ä¼šå¾ˆå¤§ï¼‰
            }
        }

        @JvmStatic
        fun showUnSupportDialog(context: Context) {
            TipDialog.Builder(context)
                .setTitle(R.string.generic_warning)
                .setMessage(R.string.select_modpack_local_not_supported) //å¼¹çª—æé†’
                .setWarning()
                .setShowCancel(true)
                .setShowConfirm(false)
                .showDialog()
        }

        @Throws(Exception::class)
        private fun curseforgeModPack(
            zipFile: File,
            versionPath: File
        ): ModLoaderWrapper? {
            return CurseForgeModPackInstallHelper.installZip(
                PlatformUtils.createCurseForgeApi(),
                zipFile,
                versionPath
            )
        }

        @Throws(Exception::class)
        private fun modrinthModPack(
            zipFile: File,
            versionPath: File
        ): ModLoaderWrapper? {
            return ModrinthModPackInstallHelper.installZip(
                zipFile,
                versionPath
            )
        }

        @Throws(Exception::class)
        private fun mcbbsModPack(context: Context, zipFile: File, versionPath: File): ModLoaderWrapper? {
            val mcbbsModPack = MCBBSModPack(context, zipFile)
            return mcbbsModPack.install(versionPath)
        }
    }
}

