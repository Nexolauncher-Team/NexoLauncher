package com.nexo.launcher.feature.version.install

import android.app.Activity
import com.nexo.launcher.mcgui.ProgressLayout
import com.nexo.launcher.R
import com.nexo.launcher.event.value.InstallGameEvent
import com.nexo.launcher.feature.log.Logging
import com.nexo.launcher.feature.version.VersionsManager
import com.nexo.launcher.task.Task
import com.nexo.launcher.Tools
import com.nexo.launcher.progresskeeper.ProgressKeeper
import com.nexo.launcher.tasks.AsyncMinecraftDownloader
import com.nexo.launcher.tasks.MinecraftDownloader
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.concurrent.atomic.AtomicReference

class GameInstaller(
    private val activity: Activity,
    installEvent: InstallGameEvent
) {
    private val realVersion: String = installEvent.minecraftVersion
    private val customVersionName: String = installEvent.customVersionName
    private val taskMap: Map<Addon, InstallTaskItem> = installEvent.taskMap
    private val targetVersionFolder = VersionsManager.getVersionPath(customVersionName)
    private val vanillaVersionFolder = VersionsManager.getVersionPath(realVersion)

    fun installGame() {
        Logging.i("Minecraft Downloader", "Start downloading the version: $realVersion")

        if (taskMap.isNotEmpty()) {
            ProgressKeeper.submitProgress(ProgressLayout.INSTALL_RESOURCE, 0, R.string.download_install_download_file, 0, 0, 0)
        }

        val mcVersion = AsyncMinecraftDownloader.getListedVersion(realVersion)
        MinecraftDownloader().start(
            mcVersion,
            realVersion,
            object : AsyncMinecraftDownloader.DoneListener {
                override fun onDownloadDone() {
                    Task.runTask {
                        if (taskMap.isEmpty()) {
                            //å¦‚æžœé™„åŠ é™„ä»¶æ˜¯ç©ºçš„ï¼Œåˆ™è¡¨æ˜Žåªéœ€è¦å®‰è£…åŽŸç‰ˆï¼Œéœ€è¦ç¡®ä¿è¿™ä¸ªè‡ªå®šä¹‰çš„ç‰ˆæœ¬æ–‡ä»¶å¤¹å†…å¿…å®šæœ‰åŽŸç‰ˆçš„.jsonæ–‡ä»¶
                            //éœ€è¦æ£€æŸ¥æ˜¯å¦è‡ªå®šä¹‰äº†ç‰ˆæœ¬åï¼Œå¦‚æžœçœŸå®žç‰ˆæœ¬ä¸Žè‡ªå®šä¹‰ç”¨æˆ·åç›¸åŒï¼Œåˆ™è¡¨ç¤ºç”¨æˆ·å¹¶æ²¡æœ‰ä¿®æ”¹ç‰ˆæœ¬åï¼Œå½“å‰å®‰è£…çš„å°±æ˜¯çº¯åŽŸç‰ˆ
                            //å¦‚æžœæ²¡æœ‰è‡ªå®šä¹‰ç”¨æˆ·åï¼Œåˆ™ä¸å¤åˆ¶ç‰ˆæœ¬æ–‡ä»¶ï¼Œæ¯•ç«ŸåŽŸç‰ˆæ–‡ä»¶ï¼Œä¸Žç›®æ ‡æ–‡ä»¶çŽ°åœ¨æ˜¯åŒä¸€ä¸ªæ–‡ä»¶ï¼
                            if (realVersion != customVersionName && VersionsManager.isVersionExists(realVersion)) {
                                //æ‰¾åˆ°åŽŸç‰ˆçš„.jsonæ–‡ä»¶ï¼Œåœ¨MinecraftDownloaderå¼€å§‹æ—¶ï¼Œå·²ç»ä¸‹è½½äº†
                                val vanillaJsonFile = File(vanillaVersionFolder, "${vanillaVersionFolder.name}.json")
                                if (vanillaJsonFile.exists() && vanillaJsonFile.isFile) {
                                    //å¦‚æžœåŽŸç‰ˆçš„.jsonæ–‡ä»¶å­˜åœ¨ï¼Œåˆ™ç›´æŽ¥å¤åˆ¶è¿‡æ¥ç”¨
                                    FileUtils.copyFile(vanillaJsonFile, File(targetVersionFolder, "$customVersionName.json"))
                                }
                            }
                            //ModLoaderä»»åŠ¡ä¸ºç©ºï¼ŒæŽ¥ä¸‹æ¥çš„æ— æ„ä¹‰ModLoaderä»»åŠ¡å°†å½»åº•è·³è¿‡ï¼
                            return@runTask null
                        }

                        //å°†Modä¸ŽModloaderçš„ä»»åŠ¡åˆ†ç¦»å‡ºæ¥ï¼Œåº”è¯¥å…ˆå®‰è£…Mod
                        val modTask: MutableList<InstallTaskItem> = ArrayList()
                        val modloaderTask = AtomicReference<Pair<Addon, InstallTaskItem>>() //æš‚æ—¶åªå…è®¸åŒæ—¶å®‰è£…ä¸€ä¸ªModLoader
                        taskMap.forEach { (addon, taskItem) ->
                            if (taskItem.isMod) modTask.add(taskItem)
                            else modloaderTask.set(Pair(addon, taskItem))
                        }

                        //ä¸‹è½½Modæ–‡ä»¶
                        modTask.forEach { task ->
                            Logging.i("Install Version", "Installing Mod: ${task.selectedVersion}")
                            val file = task.task.run(customVersionName)
                            val endTask = task.endTask
                            file?.let { endTask?.endTask(activity, it) }
                        }

                        modloaderTask.get()?.let { taskPair ->
                            ProgressKeeper.submitProgress(ProgressLayout.INSTALL_RESOURCE, 0, R.string.mod_download_progress, taskPair.first.addonName)

                            Logging.i("Install Version", "Installing ModLoader: ${taskPair.second.selectedVersion}")
                            val file = taskPair.second.task.run(customVersionName)
                            return@runTask Pair(file, taskPair.second)
                        }

                        null
                    }.ended ended@{ taskPair ->
                        taskPair?.let { pair ->
                            pair.first?.let {
                                pair.second.endTask?.endTask(activity, it)
                            }
                        }
                    }.onThrowable { e ->
                        Tools.showErrorRemote(e)
                    }.execute()
                }

                override fun onDownloadFailed(throwable: Throwable) {
                    Tools.showErrorRemote(throwable)
                    if (taskMap.isNotEmpty()) {
                        ProgressLayout.clearProgress(ProgressLayout.INSTALL_RESOURCE)
                    }
                }
            }
        )
    }
}
