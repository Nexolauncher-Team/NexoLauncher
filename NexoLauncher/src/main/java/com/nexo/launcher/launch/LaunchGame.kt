package com.nexo.launcher.launch

import android.app.Activity
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.nexo.launcher.mcgui.ProgressLayout
import com.nexo.launcher.R
import com.nexo.launcher.event.single.AccountUpdateEvent
import com.nexo.launcher.feature.accounts.AccountType
import com.nexo.launcher.feature.accounts.AccountUtils
import com.nexo.launcher.feature.accounts.AccountsManager
import com.nexo.launcher.feature.log.Logging
import com.nexo.launcher.feature.version.Version
import com.nexo.launcher.renderer.Renderers
import com.nexo.launcher.setting.AllSettings
import com.nexo.launcher.setting.AllStaticSettings
import com.nexo.launcher.support.touch_controller.ControllerProxy
import com.nexo.launcher.task.TaskExecutors
import com.nexo.launcher.ui.dialog.LifecycleAwareTipDialog
import com.nexo.launcher.ui.dialog.TipDialog
import com.nexo.launcher.utils.ZHTools
import com.nexo.launcher.utils.http.NetworkUtils
import com.nexo.launcher.utils.stringutils.StringUtils
import com.nexo.launcher.Architecture
import com.nexo.launcher.JMinecraftVersionList
import com.nexo.launcher.Logger
import com.nexo.launcher.Tools
import com.nexo.launcher.authenticator.microsoft.PresentedException
import com.nexo.launcher.lifecycle.ContextAwareDoneListener
import com.nexo.launcher.multirt.MultiRTUtils
import com.nexo.launcher.plugins.FFmpegPlugin
import com.nexo.launcher.progresskeeper.ProgressKeeper
import com.nexo.launcher.services.GameService
import com.nexo.launcher.tasks.AsyncMinecraftDownloader
import com.nexo.launcher.tasks.MinecraftDownloader
import com.nexo.launcher.utils.JREUtils
import com.nexo.launcher.value.MinecraftAccount
import org.greenrobot.eventbus.EventBus

class LaunchGame {
    companion object {
        /**
         * æ”¹ä¸ºå¯åŠ¨æ¸¸æˆå‰è¿›è¡Œçš„æ“ä½œ
         * - è¿›è¡Œç™»å½•ï¼ŒåŒæ—¶ä¹Ÿèƒ½åŠæ—¶çš„åˆ·æ–°è´¦å·çš„ä¿¡æ¯ï¼ˆè¿™æ˜Žæ˜¾æ›´åˆç†ä¸æ˜¯å—ï¼ŒPojavLauncherï¼Ÿï¼‰
         * - å¤åˆ¶ options.txt æ–‡ä»¶åˆ°æ¸¸æˆç›®å½•
         * @param version é€‰æ‹©çš„ç‰ˆæœ¬
         */
        @JvmStatic
        fun preLaunch(context: Context, version: Version) {
            val networkAvailable = NetworkUtils.isNetworkAvailable(context)

            fun launch(setOfflineAccount: Boolean = false) {
                version.offlineAccountLogin = setOfflineAccount

                val versionName = version.getVersionName()
                val mcVersion = AsyncMinecraftDownloader.getListedVersion(versionName)
                val listener = ContextAwareDoneListener(context, version)
                //è‹¥ç½‘ç»œæœªè¿žæŽ¥ï¼Œè·³è¿‡ä¸‹è½½ä»»åŠ¡ç›´æŽ¥å¯åŠ¨
                if (!networkAvailable) {
                    listener.onDownloadDone()
                } else {
                    MinecraftDownloader().start(mcVersion, versionName, listener)
                }
            }

            fun setGameProgress(pull: Boolean) {
                if (pull) {
                    ProgressKeeper.submitProgress(ProgressLayout.CHECKING_MODS, 0, R.string.mod_check_progress_message, 0, 0, 0)
                    ProgressKeeper.submitProgress(ProgressLayout.DOWNLOAD_MINECRAFT, 0, R.string.newdl_downloading_game_files, 0, 0, 0)
                } else {
                    ProgressLayout.clearProgress(ProgressLayout.DOWNLOAD_MINECRAFT)
                    ProgressLayout.clearProgress(ProgressLayout.CHECKING_MODS)
                }
            }

            if (!networkAvailable) {
                // ç½‘ç»œæœªé“¾æŽ¥ï¼Œæ— æ³•ç™»å½•ï¼Œä½†æ˜¯ä¾æ—§å…è®¸çŽ©å®¶å¯åŠ¨æ¸¸æˆ (ä¸´æ—¶åˆ›å»ºä¸€ä¸ªåŒåçš„ç¦»çº¿è´¦å·å¯åŠ¨æ¸¸æˆ)
                Toast.makeText(context, context.getString(R.string.account_login_no_network), Toast.LENGTH_SHORT).show()
                launch(true)
                return
            }

            if (AccountUtils.isNoLoginRequired(AccountsManager.currentAccount)) {
                launch()
                return
            }

            AccountsManager.performLogin(
                context, AccountsManager.currentAccount!!,
                { _ ->
                    EventBus.getDefault().post(AccountUpdateEvent())
                    TaskExecutors.runInUIThread {
                        Toast.makeText(context, context.getString(R.string.account_login_done), Toast.LENGTH_SHORT).show()
                    }
                    //ç™»å½•å®Œæˆï¼Œæ­£å¼å¯åŠ¨æ¸¸æˆï¼
                    launch()
                },
                { exception ->
                    val errorMessage = if (exception is PresentedException) exception.toString(context)
                    else exception.message

                    TaskExecutors.runInUIThread {
                        TipDialog.Builder(context)
                            .setTitle(R.string.generic_error)
                            .setMessage("${context.getString(R.string.account_login_skip)}\r\n$errorMessage")
                            .setWarning()
                            .setConfirmClickListener { launch(true) }
                            .setCenterMessage(false)
                            .showDialog()
                    }

                    setGameProgress(false)
                }
            )
            setGameProgress(true)
        }

        @Throws(Throwable::class)
        @JvmStatic
        fun runGame(activity: AppCompatActivity, minecraftVersion: Version, version: JMinecraftVersionList.Version) {
            if (!Renderers.isCurrentRendererValid()) {
                Renderers.setCurrentRenderer(activity, AllSettings.renderer.getValue())
            }

            var account = AccountsManager.currentAccount!!
            if (minecraftVersion.offlineAccountLogin) {
                account = MinecraftAccount().apply {
                    this.username = account.username
                    this.accountType = AccountType.LOCAL.type
                }
            }

            val customArgs = minecraftVersion.getJavaArgs().takeIf { it.isNotBlank() } ?: ""

            val javaRuntime = getRuntime(activity, minecraftVersion, version.javaVersion?.majorVersion ?: 8)

            printLauncherInfo(
                minecraftVersion,
                customArgs.takeIf { it.isNotBlank() } ?: "NONE",
                javaRuntime,
                account
            )

            minecraftVersion.modCheckResult?.let { modCheckResult ->
                if (modCheckResult.hasTouchController) {
                    Logger.appendToLog("Mod Perception: TouchController Mod found, attempting to automatically enable control proxy!")
                    ControllerProxy.startProxy(activity)
                    AllStaticSettings.useControllerProxy = true
                }

                if (modCheckResult.hasSodiumOrEmbeddium) {
                    Logger.appendToLog("Mod Perception: Sodium or Embeddium Mod found, attempting to load the disable warning tool later!")
                }
            }

            JREUtils.redirectAndPrintJRELog()

            launch(activity, account, minecraftVersion, javaRuntime, customArgs)

            //Note that we actually stall in the above function, even if the game crashes. But let's be safe.
            GameService.setActive(false)
        }

        private fun getRuntime(activity: Activity, version: Version, targetJavaVersion: Int): String {
            val versionRuntime = version.getJavaDir()
                .takeIf { it.isNotEmpty() && it.startsWith(Tools.LAUNCHERPROFILES_RTPREFIX) }
                ?.removePrefix(Tools.LAUNCHERPROFILES_RTPREFIX)
                ?: ""

            if (versionRuntime.isNotEmpty()) return versionRuntime

            //å¦‚æžœç‰ˆæœ¬æœªé€‰æ‹©JavaçŽ¯å¢ƒï¼Œåˆ™è‡ªåŠ¨é€‰æ‹©åˆé€‚çš„çŽ¯å¢ƒ
            var runtime = AllSettings.defaultRuntime.getValue()
            val pickedRuntime = MultiRTUtils.read(runtime)
            if (pickedRuntime.javaVersion == 0 || pickedRuntime.javaVersion < targetJavaVersion) {
                runtime = MultiRTUtils.getNearestJreName(targetJavaVersion) ?: run {
                    activity.runOnUiThread {
                        Toast.makeText(activity, activity.getString(R.string.game_autopick_runtime_failed), Toast.LENGTH_SHORT).show()
                    }
                    return runtime
                }
            }
            return runtime
        }

        private fun printLauncherInfo(
            minecraftVersion: Version,
            javaArguments: String,
            javaRuntime: String,
            account: MinecraftAccount
        ) {
            var mcInfo = minecraftVersion.getVersionName()
            minecraftVersion.getVersionInfo()?.let { info ->
                mcInfo = info.getInfoString()
            }

            Logger.appendToLog("--------- Start launching the game")
            Logger.appendToLog("Info: Launcher version: ${ZHTools.getVersionName()} (${ZHTools.getVersionCode()})")
            Logger.appendToLog("Info: Architecture: ${Architecture.archAsString(Tools.DEVICE_ARCHITECTURE)}")
            Logger.appendToLog("Info: Device model: ${StringUtils.insertSpace(Build.MANUFACTURER, Build.MODEL)}")
            Logger.appendToLog("Info: API version: ${Build.VERSION.SDK_INT}")
            Logger.appendToLog("Info: Renderer: ${Renderers.getCurrentRenderer().getRendererName()}")
            Logger.appendToLog("Info: Selected Minecraft version: ${minecraftVersion.getVersionName()}")
            Logger.appendToLog("Info: Minecraft Info: $mcInfo")
            Logger.appendToLog("Info: Game Path: ${minecraftVersion.getGameDir().absolutePath} (Isolation: ${minecraftVersion.isIsolation()})")
            Logger.appendToLog("Info: Custom Java arguments: $javaArguments")
            Logger.appendToLog("Info: Java Runtime: $javaRuntime")
            Logger.appendToLog("Info: Account: ${account.username} (${account.accountType})")
            Logger.appendToLog("---------\r\n")
        }

        @Throws(Throwable::class)
        @JvmStatic
        private fun launch(
            activity: AppCompatActivity,
            account: MinecraftAccount,
            minecraftVersion: Version,
            javaRuntime: String,
            customArgs: String
        ) {
            checkMemory(activity)

            val runtime = MultiRTUtils.forceReread(javaRuntime)

            val versionInfo = Tools.getVersionInfo(minecraftVersion)
            val gameDirPath = minecraftVersion.getGameDir()

            //é¢„å¤„ç†
            Tools.disableSplash(gameDirPath)
            val launchClassPath = Tools.generateLaunchClassPath(versionInfo, minecraftVersion)

            val launchArgs = LaunchArgs(
                account,
                gameDirPath,
                minecraftVersion,
                versionInfo,
                minecraftVersion.getVersionName(),
                runtime,
                launchClassPath
            ).getAllArgs()

            FFmpegPlugin.discover(activity)

            JREUtils.launchWithUtils(activity, runtime, minecraftVersion, launchArgs, customArgs)
        }

        private fun checkMemory(activity: AppCompatActivity) {
            var freeDeviceMemory = Tools.getFreeDeviceMemory(activity)
            val freeAddressSpace =
                if (Architecture.is32BitsDevice())
                    Tools.getMaxContinuousAddressSpaceSize()
                else -1
            Logging.i("MemStat",
                "Free RAM: $freeDeviceMemory Addressable: $freeAddressSpace")

            val stringId: Int = if (freeDeviceMemory > freeAddressSpace && freeAddressSpace != -1) {
                freeDeviceMemory = freeAddressSpace
                R.string.address_memory_warning_msg
            } else R.string.memory_warning_msg

            if (AllSettings.ramAllocation.value.getValue() > freeDeviceMemory) {
                val builder = TipDialog.Builder(activity)
                    .setTitle(R.string.generic_warning)
                    .setMessage(activity.getString(stringId, freeDeviceMemory, AllSettings.ramAllocation.value.getValue()))
                    .setWarning()
                    .setCenterMessage(false)
                    .setShowCancel(false)
                if (LifecycleAwareTipDialog.haltOnDialog(activity.lifecycle, builder)) return
                // If the dialog's lifecycle has ended, return without
                // actually launching the game, thus giving us the opportunity
                // to start after the activity is shown again
            }
        }
    }
}
