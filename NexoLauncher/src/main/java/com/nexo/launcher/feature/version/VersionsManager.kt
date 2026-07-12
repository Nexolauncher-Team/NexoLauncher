package com.nexo.launcher.feature.version

import android.content.Context
import com.nexo.launcher.InfoDistributor
import com.nexo.launcher.R
import com.nexo.launcher.event.single.RefreshVersionsEvent
import com.nexo.launcher.event.single.RefreshVersionsEvent.MODE.END
import com.nexo.launcher.event.single.RefreshVersionsEvent.MODE.START
import com.nexo.launcher.feature.customprofilepath.ProfilePathHome
import com.nexo.launcher.feature.log.Logging
import com.nexo.launcher.feature.version.favorites.FavoritesVersionUtils
import com.nexo.launcher.feature.version.utils.VersionInfoUtils
import com.nexo.launcher.task.Task
import com.nexo.launcher.task.TaskExecutors
import com.nexo.launcher.ui.dialog.EditTextDialog
import com.nexo.launcher.utils.ZHTools
import com.nexo.launcher.utils.file.FileTools
import com.nexo.launcher.utils.stringutils.SortStrings
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.nexo.launcher.Tools
import org.apache.commons.io.FileUtils
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

/**
 * æ‰€æœ‰ç‰ˆæœ¬ç®¡ç†è€…
 * @see Version
 */
object VersionsManager {
    private val versions = CopyOnWriteArrayList<Version>()

    /**
     * @return èŽ·å–å½“å‰çš„æ¸¸æˆä¿¡æ¯
     */
    lateinit var currentGameInfo: CurrentGameInfo
        private set

    private val coroutineScope = CoroutineScope(Dispatchers.IO + CoroutineName("VersionsManager"))
    private val refreshMutex = Mutex()
    private var isRefreshing: Boolean = false
    private var lastRefreshTime = 0L

    /**
     * @return æ£€æŸ¥æ˜¯å¦å¯ä»¥åˆ·æ–°
     */
    @JvmStatic
    fun canRefresh() = !isRefreshing && ZHTools.getCurrentTimeMillis() - lastRefreshTime > 500

    /**
     * @return å…¨éƒ¨çš„ç‰ˆæœ¬æ•°æ®
     */
    fun getVersions() = versions.toList()

    /**
     * æ£€æŸ¥ç‰ˆæœ¬æ˜¯å¦å·²ç»å­˜åœ¨
     */
    fun isVersionExists(versionName: String, checkJson: Boolean = false): Boolean {
        val folder = File(ProfilePathHome.getVersionsHome(), versionName)
        //ä¿è¯ç‰ˆæœ¬æ–‡ä»¶å¤¹å­˜åœ¨çš„åŒæ—¶ï¼Œä¹Ÿåº”ä¿è¯å…¶ç‰ˆæœ¬jsonæ–‡ä»¶å­˜åœ¨
        return if (checkJson) File(folder, "${folder.name}.json").exists()
        else folder.exists()
    }

    /**
     * å¼‚æ­¥åˆ·æ–°å½“å‰çš„ç‰ˆæœ¬åˆ—è¡¨ï¼Œåˆ·æ–°å®ŒæˆåŽï¼Œå°†ä½¿ç”¨ä¸€ä¸ªäº‹ä»¶è¿›è¡Œé€šçŸ¥ï¼Œä¸è¿‡è¿™ä¸ªäº‹ä»¶å¹¶ä¸ä¼šåœ¨UIçº¿ç¨‹æ‰§è¡Œ
     * @param tag æ ‡è®°æ˜¯è°å‘èµ·äº†ç‰ˆæœ¬åˆ·æ–°ä»»åŠ¡ï¼Œæ–¹ä¾¿debug
     * @see com.nexo.launcher.event.single.RefreshVersionsEvent
     */
    fun refresh(tag: String, refreshVersionInfo: Boolean = false) {
        Logging.i("VersionsManager", "$tag initiated the refresh version task")
        coroutineScope.launch {
            refreshMutex.withLock {
                lastRefreshTime = ZHTools.getCurrentTimeMillis()
                handleRefreshOperation(refreshVersionInfo)
            }
        }
    }

    private fun handleRefreshOperation(refreshVersionInfo: Boolean) {
        isRefreshing = true
        EventBus.getDefault().post(RefreshVersionsEvent(START))

        versions.clear()

        val versionsHome: String = ProfilePathHome.getVersionsHome()
        File(versionsHome).listFiles()?.forEach { versionFile ->
            runCatching {
                processVersionFile(versionsHome, versionFile, refreshVersionInfo)
            }
        }

        versions.sortWith { o1, o2 ->
            var sort = -SortStrings.compareClassVersions(
                o1.getVersionInfo()?.minecraftVersion ?: o1.getVersionName(),
                o2.getVersionInfo()?.minecraftVersion ?: o2.getVersionName()
            )
            if (sort == 0) sort = SortStrings.compareChar(o1.getVersionName(), o2.getVersionName())
            sort
        }

        currentGameInfo = CurrentGameInfo.refreshCurrentInfo()

        //ä½¿ç”¨äº‹ä»¶é€šçŸ¥ç‰ˆæœ¬å·²åˆ·æ–°
        EventBus.getDefault().post(RefreshVersionsEvent(END))
        isRefreshing = false
    }

    private fun processVersionFile(versionsHome: String, versionFile: File, refreshVersionInfo: Boolean) {
        if (versionFile.exists() && versionFile.isDirectory) {
            var isVersion = false

            //é€šè¿‡åˆ¤æ–­æ˜¯å¦å­˜åœ¨ç‰ˆæœ¬çš„.jsonæ–‡ä»¶ï¼Œæ¥ç¡®å®šå…¶æ˜¯å¦ä¸ºä¸€ä¸ªç‰ˆæœ¬
            val jsonFile = File(versionFile, "${versionFile.name}.json")
            if (jsonFile.exists() && jsonFile.isFile) {
                isVersion = true
                val versionInfoFile = File(getNexoVersionPath(versionFile), "VersionInfo.json")
                if (refreshVersionInfo) FileUtils.deleteQuietly(versionInfoFile)
                if (!versionInfoFile.exists()) {
                    VersionInfoUtils.parseJson(jsonFile)?.save(versionFile)
                }
            }

            val versionConfig = VersionConfig.parseConfig(versionFile)

            val version = Version(
                versionsHome,
                versionFile.absolutePath,
                versionConfig,
                isVersion
            )
            versions.add(version)

            Logging.i("VersionsManager", "Identified and added version: ${version.getVersionName()}, " +
                    "Path: (${version.getVersionPath()}), " +
                    "Info: ${version.getVersionInfo()?.getInfoString()}")
        }
    }

    /**
     * @return èŽ·å–å½“å‰çš„ç‰ˆæœ¬
     */
    fun getCurrentVersion(): Version? {
        if (versions.isEmpty()) return null

        fun returnVersionByFirst(): Version? {
            return versions.find { it.isValid() }?.apply {
                //ç¡®ä¿ç‰ˆæœ¬æœ‰æ•ˆ
                saveCurrentVersion(getVersionName())
            }
        }

        return runCatching {
            val versionString = currentGameInfo.version
            getVersion(versionString) ?: run {
                return returnVersionByFirst()
            }
        }.getOrElse { e ->
            Logging.e("Get Current Version", Tools.printToString(e))
            returnVersionByFirst()
        }
    }

    /**
     * @return é€šè¿‡ç‰ˆæœ¬åï¼Œåˆ¤æ–­å…¶ç‰ˆæœ¬æ˜¯å¦å­˜åœ¨
     */
    fun checkVersionExistsByName(versionName: String?) =
        versionName?.let { name -> versions.any { it.getVersionName() == name } } ?: false

    /**
     * @return èŽ·å– Nexo å¯åŠ¨å™¨ç‰ˆæœ¬æ ‡è¯†æ–‡ä»¶å¤¹
     */
    fun getNexoVersionPath(version: Version) = File(version.getVersionPath(), InfoDistributor.LAUNCHER_NAME)

    /**
     * @return é€šè¿‡ç›®å½•èŽ·å– Nexo å¯åŠ¨å™¨ç‰ˆæœ¬æ ‡è¯†æ–‡ä»¶å¤¹
     */
    fun getNexoVersionPath(folder: File) = File(folder, InfoDistributor.LAUNCHER_NAME)

    /**
     * @return é€šè¿‡åç§°èŽ·å– Nexo å¯åŠ¨å™¨ç‰ˆæœ¬æ ‡è¯†æ–‡ä»¶å¤¹
     */
    fun getNexoVersionPath(name: String) = File(getVersionPath(name), InfoDistributor.LAUNCHER_NAME)

    /**
     * @return èŽ·å–å½“å‰ç‰ˆæœ¬è®¾ç½®çš„å›¾æ ‡
     */
    fun getVersionIconFile(version: Version) = File(getNexoVersionPath(version), "VersionIcon.png")

    /**
     * @return é€šè¿‡åç§°èŽ·å–å½“å‰ç‰ˆæœ¬è®¾ç½®çš„å›¾æ ‡
     */
    fun getVersionIconFile(name: String) = File(getNexoVersionPath(name), "VersionIcon.png")

    /**
     * @return é€šè¿‡åç§°èŽ·å–ç‰ˆæœ¬çš„æ–‡ä»¶å¤¹è·¯å¾„
     */
    fun getVersionPath(name: String) = File(ProfilePathHome.getVersionsHome(), name)

    /**
     * ä¿å­˜å½“å‰é€‰æ‹©çš„ç‰ˆæœ¬
     */
    fun saveCurrentVersion(versionName: String) {
        runCatching {
            currentGameInfo.apply {
                version = versionName
                saveCurrentInfo()
            }
        }.onFailure { e -> Logging.e("Save Current Version", Tools.printToString(e)) }
    }

    private fun validateVersionName(
        context: Context,
        newName: String,
        versionInfo: VersionInfo?
    ): String? {
        return when {
            isVersionExists(newName, true) ->
                context.getString(R.string.version_install_exists)
            versionInfo?.loaderInfo?.takeIf { it.isNotEmpty() }?.let {
                //å¦‚æžœè¿™ä¸ªç‰ˆæœ¬æ˜¯æœ‰ModLoaderåŠ è½½å™¨ä¿¡æ¯çš„ï¼Œåˆ™ä¸å…è®¸ä¿®æ”¹ä¸ºä¸ŽåŽŸç‰ˆåç§°ä¸€è‡´çš„åç§°ï¼Œé˜²æ­¢å†²çª
                newName == versionInfo.minecraftVersion
            } ?: false ->
                context.getString(R.string.version_install_cannot_use_mc_name)
            else -> null
        }
    }

    /**
     * æ‰“å¼€é‡å‘½åç‰ˆæœ¬çš„å¼¹çª—ï¼Œéœ€è¦ç¡®ä¿åœ¨UIçº¿ç¨‹è¿è¡Œ
     * @param beforeRename åœ¨é‡å‘½åå‰ä¸€æ­¥çš„æ“ä½œ
     */
    fun openRenameDialog(context: Context, version: Version, beforeRename: (() -> Unit)? = null) {
        EditTextDialog.Builder(context)
            .setTitle(R.string.version_manager_rename)
            .setEditText(version.getVersionName())
            .setAsRequired()
            .setConfirmListener { editText, _ ->
                val string = editText.text.toString()

                //ä¸ŽåŽŸå§‹åç§°ä¸€è‡´
                if (string == version.getVersionName()) return@setConfirmListener true

                if (FileTools.isFilenameInvalid(editText)) {
                    return@setConfirmListener false
                }

                val error = validateVersionName(context, string, version.getVersionInfo())
                error?.let {
                    editText.error = it
                    return@setConfirmListener false
                }

                beforeRename?.invoke()
                renameVersion(version, string)

                true
            }.showDialog()
    }

    /**
     * é‡å‘½åå½“å‰ç‰ˆæœ¬ï¼Œä½†å¹¶ä¸ä¼šåœ¨è¿™é‡Œå¯¹å³å°†é‡å‘½åçš„åç§°ï¼Œè¿›è¡Œéžæ³•æ€§åˆ¤æ–­
     */
    private fun renameVersion(version: Version, name: String) {
        val currentVersionName = getCurrentVersion()?.getVersionName()
        //å¦‚æžœå½“å‰çš„ç‰ˆæœ¬æ˜¯å³å°†è¢«é‡å‘½åçš„ç‰ˆæœ¬ï¼Œé‚£ä¹ˆå°±æŠŠå°†è¦é‡å‘½åçš„åå­—è®¾ç½®ä¸ºå½“å‰ç‰ˆæœ¬
        if (version.getVersionName() == currentVersionName) saveCurrentVersion(name)

        //å°è¯•åˆ·æ–°æ”¶è—å¤¹å†…çš„ç‰ˆæœ¬åç§°
        FavoritesVersionUtils.renameVersion(version.getVersionName(), name)

        val versionFolder = version.getVersionPath()
        val renameFolder = File(ProfilePathHome.getVersionsHome(), name)

        //ä¸ç®¡é‡å‘½åä¹‹åŽçš„æ–‡ä»¶å¤¹æ˜¯ä»€ä¹ˆï¼Œåªè¦è¿™ä¸ªæ–‡ä»¶å¤¹å­˜åœ¨ï¼Œé‚£ä¹ˆå°±å¿…é¡»åˆ é™¤
        //å¦åˆ™å°†å‡ºçŽ°é—®é¢˜
        FileUtils.deleteQuietly(renameFolder)

        val originalName = versionFolder.name

        FileTools.renameFile(versionFolder, renameFolder)

        val versionJsonFile = File(renameFolder, "$originalName.json")
        val versionJarFile = File(renameFolder, "$originalName.jar")
        val renameJsonFile = File(renameFolder, "$name.json")
        val renameJarFile = File(renameFolder, "$name.jar")

        FileTools.renameFile(versionJsonFile, renameJsonFile)
        FileTools.renameFile(versionJarFile, renameJarFile)

        FileUtils.deleteQuietly(versionFolder)

        //é‡å‘½ååŽï¼Œéœ€è¦åˆ·æ–°åˆ—è¡¨
        refresh("VersionsManager:renameVersion")
    }

    /**
     * æ‰“å¼€å¤åˆ¶ç‰ˆæœ¬çš„åç§°è¾“å…¥æ¡†ï¼Œå°†é€‰ä¸­çš„ç‰ˆæœ¬å¤åˆ¶ä¸ºä¸€ä¸ªæ–°çš„ç‰ˆæœ¬
     */
    fun openCopyDialog(context: Context, version: Version) {
        val dialog = ZHTools.createTaskRunningDialog(context)
        EditTextDialog.Builder(context)
            .setTitle(R.string.version_manager_copy)
            .setMessage(R.string.version_manager_copy_tip)
            .setCheckBoxText(R.string.version_manager_copy_all)
            .setShowCheckBox(true)
            .setEditText(version.getVersionName())
            .setAsRequired()
            .setConfirmListener { editText, checked ->
                val string = editText.text.toString()

                //ä¸ŽåŽŸå§‹åç§°ä¸€è‡´
                if (string == version.getVersionName()) return@setConfirmListener true

                if (FileTools.isFilenameInvalid(editText)) {
                    return@setConfirmListener false
                }

                val error = validateVersionName(context, string, version.getVersionInfo())
                error?.let {
                    editText.error = it
                    return@setConfirmListener false
                }

                Task.runTask {
                    copyVersion(version, string, checked)
                }.beforeStart(TaskExecutors.getAndroidUI()) {
                    dialog.show()
                }.onThrowable { e ->
                    Tools.showErrorRemote(e)
                }.finallyTask(TaskExecutors.getAndroidUI()) {
                    dialog.dismiss()
                    refresh("VersionsManager:openCopyDialog")
                }.execute()
                true
            }.showDialog()
    }

    /**
     * å°†é€‰ä¸­çš„ç‰ˆæœ¬å¤åˆ¶ä¸ºä¸€ä¸ªæ–°çš„ç‰ˆæœ¬
     * @param version é€‰ä¸­çš„ç‰ˆæœ¬
     * @param name æ–°çš„ç‰ˆæœ¬çš„åç§°
     * @param copyAllFile æ˜¯å¦å¤åˆ¶å…¨éƒ¨æ–‡ä»¶
     */
    private fun copyVersion(version: Version, name: String, copyAllFile: Boolean) {
        val versionsFolder = version.getVersionsFolder()
        val newVersion = File(versionsFolder, name)

        val originalName = version.getVersionName()

        //æ–°ç‰ˆæœ¬çš„jsonä¸Žjaræ–‡ä»¶
        val newJsonFile = File(newVersion, "$name.json")
        val newJarFile = File(newVersion, "$name.jar")

        val originalVersionFolder = version.getVersionPath()
        if (copyAllFile) {
            //å¯ç”¨å¤åˆ¶æ‰€æœ‰æ–‡ä»¶æ—¶ï¼Œç›´æŽ¥å°†åŽŸæ–‡ä»¶å¤¹æ•´ä½“å¤åˆ¶åˆ°æ–°ç‰ˆæœ¬
            FileUtils.copyDirectory(originalVersionFolder, newVersion)
            //é‡å‘½åjsonã€jaræ–‡ä»¶
            val jsonFile = File(newVersion, "$originalName.json")
            val jarFile = File(newVersion, "$originalName.jar")
            if (jsonFile.exists()) jsonFile.renameTo(newJsonFile)
            if (jarFile.exists()) jarFile.renameTo(newJarFile)
        } else {
            //ä¸å¤åˆ¶æ‰€æœ‰æ–‡ä»¶æ—¶ï¼Œä»…å¤åˆ¶å¹¶é‡å‘½åjsonã€jaræ–‡ä»¶
            val originalJsonFile = File(originalVersionFolder, "$originalName.json")
            val originalJarFile = File(originalVersionFolder, "$originalName.jar")
            newVersion.mkdirs()
            // versions/1.21.3/1.21.3.json -> versions/name/name.json
            if (originalJsonFile.exists()) originalJsonFile.copyTo(newJsonFile)
            // versions/1.21.3/1.21.3.jar -> versions/name/name.jar
            if (originalJarFile.exists()) originalJarFile.copyTo(newJarFile)
        }

        //ä¿å­˜ç‰ˆæœ¬é…ç½®æ–‡ä»¶
        version.getVersionConfig().copy().let { config ->
            config.setVersionPath(newVersion)
            config.setIsolationType(VersionConfig.IsolationType.ENABLE)
            config.saveWithThrowable()
        }
    }

    private fun getVersion(name: String?): Version? {
        name?.let { versionName ->
            return versions.find { it.getVersionName() == versionName }?.takeIf { it.isValid() }
        }
        return null
    }
}
