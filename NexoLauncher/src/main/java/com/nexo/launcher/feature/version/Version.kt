package com.nexo.launcher.feature.version

import android.os.Parcel
import android.os.Parcelable
import com.nexo.launcher.feature.customprofilepath.ProfilePathHome
import com.nexo.launcher.feature.mod.parser.ModChecker
import com.nexo.launcher.setting.AllSettings
import com.nexo.launcher.utils.ZHTools
import com.nexo.launcher.utils.path.PathManager
import com.nexo.launcher.Tools
import java.io.File

/**
 * Minecraft ç‰ˆæœ¬ï¼Œç”±ç‰ˆæœ¬åç§°è¿›è¡ŒåŒºåˆ†
 * @param versionsFolder ç‰ˆæœ¬æ‰€å±žçš„ç‰ˆæœ¬æ–‡ä»¶å¤¹
 * @param versionPath ç‰ˆæœ¬çš„è·¯å¾„
 * @param versionConfig ç‹¬ç«‹ç‰ˆæœ¬çš„é…ç½®
 * @param isValid ç‰ˆæœ¬çš„æœ‰æ•ˆæ€§
 */
class Version(
    private val versionsFolder: String,
    private val versionPath: String,
    private val versionConfig: VersionConfig,
    private val isValid: Boolean
) :Parcelable {
    /**
     * æŽ§åˆ¶æ˜¯å¦å°†å½“å‰è´¦å·è§†ä¸ºç¦»çº¿è´¦å·å¯åŠ¨æ¸¸æˆ
     */
    var offlineAccountLogin: Boolean = false

    /**
     * æ¨¡ç»„æ£€æŸ¥ç»“æžœ
     */
    var modCheckResult: ModChecker.ModCheckResult? = null

    /**
     * @return èŽ·å–ç‰ˆæœ¬æ‰€å±žçš„ç‰ˆæœ¬æ–‡ä»¶å¤¹
     */
    fun getVersionsFolder(): String = versionsFolder

    /**
     * @return èŽ·å–ç‰ˆæœ¬æ–‡ä»¶å¤¹
     */
    fun getVersionPath(): File = File(versionPath)

    /**
     * @return èŽ·å–ç‰ˆæœ¬åç§°
     */
    fun getVersionName(): String = getVersionPath().name

    /**
     * @return èŽ·å–ç‰ˆæœ¬éš”ç¦»é…ç½®
     */
    fun getVersionConfig() = versionConfig

    /**
     * @return ç‰ˆæœ¬çš„æœ‰æ•ˆæ€§ï¼šæ˜¯å¦å­˜åœ¨ç‰ˆæœ¬JSONæ–‡ä»¶ã€ç‰ˆæœ¬æ–‡ä»¶å¤¹æ˜¯å¦å­˜åœ¨
     */
    fun isValid() = isValid && getVersionPath().exists()

    /**
     * @return æ˜¯å¦å¼€å¯äº†ç‰ˆæœ¬éš”ç¦»
     */
    fun isIsolation() = versionConfig.isIsolation()

    /**
     * @return èŽ·å–ç‰ˆæœ¬çš„æ¸¸æˆæ–‡ä»¶å¤¹è·¯å¾„ï¼ˆè‹¥å¼€å¯äº†ç‰ˆæœ¬éš”ç¦»ï¼Œåˆ™è·¯å¾„ä¸ºç‰ˆæœ¬æ–‡ä»¶å¤¹ï¼‰
     */
    fun getGameDir(): File {
        return if (versionConfig.isIsolation()) versionConfig.getVersionPath()
        //æœªå¼€å¯ç‰ˆæœ¬éš”ç¦»å¯ä»¥ä½¿ç”¨è‡ªå®šä¹‰è·¯å¾„ï¼Œå¦‚æžœè‡ªå®šä¹‰è·¯å¾„ä¸ºç©ºï¼ˆåˆ™ä¸ºæœªè®¾ç½®ï¼‰ï¼Œé‚£ä¹ˆè¿”å›žé»˜è®¤æ¸¸æˆè·¯å¾„ï¼ˆ.minecraft/ï¼‰
        else if (versionConfig.getCustomPath().isNotEmpty()) File(versionConfig.getCustomPath())
        else File(ProfilePathHome.getGameHome())
    }

    private fun String.getValueOrDefault(default: String): String = this.takeIf { it.isNotEmpty() } ?: default

    fun getRenderer(): String = versionConfig.getRenderer().getValueOrDefault(AllSettings.renderer.getValue())

    fun getDriver(): String = versionConfig.getDriver().getValueOrDefault(AllSettings.driver.getValue())

    fun getJavaDir(): String = versionConfig.getJavaDir().getValueOrDefault(AllSettings.defaultRuntime.getValue())

    fun getJavaArgs(): String = versionConfig.getJavaArgs().getValueOrDefault(AllSettings.javaArgs.getValue())

    fun getControl(): String {
        val configControl = versionConfig.getControl().removeSuffix("./")
        return if (configControl.isNotEmpty()) File(PathManager.DIR_CTRLMAP_PATH, configControl).absolutePath
        else File(AllSettings.defaultCtrl.getValue()).absolutePath
    }

    fun getCustomInfo(): String = versionConfig.getCustomInfo().getValueOrDefault(AllSettings.versionCustomInfo.getValue())
        .replace("[zl_version]", ZHTools.getVersionName())

    fun getVersionInfo(): VersionInfo? {
        return runCatching {
            val infoFile = File(VersionsManager.getNexoVersionPath(this), "VersionInfo.json")
            Tools.GLOBAL_GSON.fromJson(Tools.read(infoFile), VersionInfo::class.java)
        }.getOrElse { null }
    }

    private fun Boolean.getInt(): Int = if (this) 1 else 0

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeStringList(listOf(versionsFolder, versionPath))
        dest.writeParcelable(versionConfig, flags)
        dest.writeInt(isValid.getInt())
        dest.writeInt(offlineAccountLogin.getInt())
        dest.writeParcelable(modCheckResult, flags)
    }

    companion object CREATOR : Parcelable.Creator<Version> {
        private fun Int.toBoolean(): Boolean = this != 0

        override fun createFromParcel(parcel: Parcel): Version {
            val stringList = ArrayList<String>()
            parcel.readStringList(stringList)
            val versionConfig = parcel.readParcelable<VersionConfig>(VersionConfig::class.java.classLoader)!!
            val isValid = parcel.readInt().toBoolean()
            val offlineAccount = parcel.readInt().toBoolean()
            val modCheckResult = parcel.readParcelable<ModChecker.ModCheckResult>(ModChecker.ModCheckResult::class.java.classLoader)

            return Version(stringList[0], stringList[1], versionConfig, isValid).apply {
                offlineAccountLogin = offlineAccount
                this.modCheckResult = modCheckResult
            }
        }

        override fun newArray(size: Int): Array<Version?> {
            return arrayOfNulls(size)
        }
    }
}
