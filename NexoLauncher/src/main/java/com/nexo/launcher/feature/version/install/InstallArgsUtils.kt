package com.nexo.launcher.feature.version.install

import android.content.Intent
import com.google.gson.JsonParser
import com.nexo.launcher.mcgui.ProgressLayout
import com.nexo.launcher.R
import com.nexo.launcher.feature.customprofilepath.ProfilePathHome
import com.nexo.launcher.utils.path.LibPath
import com.nexo.launcher.JavaGUILauncherActivity
import com.nexo.launcher.progresskeeper.ProgressKeeper
import java.io.File
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

class InstallArgsUtils(private val mcVersion: String, private val loaderVersion: String) {
    fun setFabric(intent: Intent, jarFile: File, customName: String) {
        val args = "-DprofileName=\"$customName\" -javaagent:${LibPath.MIO_FABRIC_AGENT.absolutePath}" +
                " -jar ${jarFile.absolutePath} client -mcversion \"$mcVersion\" -loader \"$loaderVersion\" -dir \"${ProfilePathHome.getGameHome()}\""
        intent.putExtra("javaArgs", args)
        intent.putExtra(JavaGUILauncherActivity.SUBSCRIBE_JVM_EXIT_EVENT, true)
        intent.putExtra(JavaGUILauncherActivity.FORCE_SHOW_LOG, true)
    }

    @Deprecated("ä¸æ”¯æŒJRE 8è¿›è¡Œå®‰è£…ï¼Œæ›´é«˜çš„JREçŽ¯å¢ƒå®‰è£…æ—¶ï¼Œä¸ä¼šè‡ªåŠ¨é€€å‡ºï¼Œå› æ­¤æš‚æ—¶ä¸ä½¿ç”¨è¿™ä¸ªå‡½æ•°è¿›è¡Œé…ç½®å®‰è£…")
    fun setQuilt(intent: Intent, jarFile: File) {
        val args = "-jar ${jarFile.absolutePath} install client \"$mcVersion\" \"$loaderVersion\" --install-dir=\"${ProfilePathHome.getGameHome()}\""
        intent.putExtra("javaArgs", args)
        intent.putExtra(JavaGUILauncherActivity.SUBSCRIBE_JVM_EXIT_EVENT, true)
        intent.putExtra(JavaGUILauncherActivity.FORCE_SHOW_LOG, true)
    }

    @Throws(Throwable::class)
    fun setForge(intent: Intent, jarFile: File, customName: String) {
        forgeLikeCustomVersionName(jarFile, customName)

        val args = "-javaagent:${LibPath.FORGE_INSTALLER.absolutePath}=\"$loaderVersion\" -jar ${jarFile.absolutePath}"
        intent.putExtra("javaArgs", args)
    }

    @Throws(Throwable::class)
    fun setNeoForge(intent: Intent, jarFile: File, customName: String) {
        forgeLikeCustomVersionName(jarFile, customName)

        val args = "-jar ${jarFile.absolutePath} --installClient \"${ProfilePathHome.getGameHome()}\""
        intent.putExtra("javaArgs", args)
        intent.putExtra(JavaGUILauncherActivity.SUBSCRIBE_JVM_EXIT_EVENT, true)
        intent.putExtra(JavaGUILauncherActivity.FORCE_SHOW_LOG, true)
        intent.putExtra("disableSecurityManager", true)
    }

    fun setOptiFine(intent: Intent, jarFile: File, customName: String) {
        val args = "-javaagent:${LibPath.FORGE_INSTALLER.absolutePath}=OFNPS " +
                "-javaagent:${LibPath.OPTIFINE_RENAMER.absolutePath}=\"$customName\" " +
                "-jar ${jarFile.absolutePath}"
        intent.putExtra("javaArgs", args)
    }

    /**
     * å°†Forgeæˆ–NeoForgeå®‰è£…å™¨ä¸­çš„install_profile.json æ–‡ä»¶ä¸­çš„ version çš„é”®ï¼Œä¿®æ”¹ä¸º customName
     * Forgeå®‰è£…å™¨ä¼šæ ¹æ® version è¿™ä¸ªå€¼ï¼Œæ¥ç”Ÿæˆå¯¹åº”çš„ç‰ˆæœ¬æ–‡ä»¶å¤¹
     * è¿™æ ·åšæ˜¯ä¸ºäº†è‡ªå®šä¹‰ç‰ˆæœ¬ json çš„å®‰è£…ä½ç½®
     */
    @Throws(Throwable::class)
    private fun forgeLikeCustomVersionName(jarFile: File, customName: String) {
        val tempJarFile = File(jarFile.parentFile, "${jarFile.nameWithoutExtension}_temp.jar")
        val profileJson = File(jarFile.parentFile, "install_profile.json")
        try {
            updateProgress(0)

            if (tempJarFile.exists()) tempJarFile.delete()
            extractInstallProfile(jarFile, profileJson)
            updateProgress(50)

            modifyJsonFile(profileJson, customName)
            writeTempJarFile(jarFile, tempJarFile, profileJson)
            updateProgress(100)

            if (!jarFile.delete()) throw IOException("Failed to delete original Installer file!")
            if (!tempJarFile.renameTo(jarFile)) throw IOException("Failed to rename temp Installer file to original!")
            profileJson.delete()
        } catch (e: Exception) {
            throw RuntimeException(e)
        } finally {
            ProgressLayout.clearProgress(ProgressLayout.INSTALL_RESOURCE)
        }
    }

    private fun updateProgress(progress: Int) {
        ProgressKeeper.submitProgress(ProgressLayout.INSTALL_RESOURCE, progress, R.string.mod_forge_custom_version)
    }

    /**
     * è§£åŽ‹å‡ºinstall_profile.json
     */
    @Throws(Throwable::class)
    private fun extractInstallProfile(jarFile: File, profileJson: File) {
        val zipFile = ZipFile(jarFile)
        val entry = zipFile.getEntry("install_profile.json")
            ?: throw IOException("File \"install_profile.json\" not found in the Installer")
        profileJson.outputStream().use { outputStream ->
            zipFile.getInputStream(entry).use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    /**
     * é€šè¿‡ä¿®æ”¹install_profile.jsonæ–‡ä»¶ä¸­çš„å€¼ï¼Œæ¥å®žçŽ°è‡ªå®šä¹‰ç‰ˆæœ¬åç§°çš„æ•ˆæžœ
     */
    @Throws(Throwable::class)
    private fun modifyJsonFile(profileJson: File, customName: String) {
        val jsonObject = JsonParser.parseString(profileJson.readText()).asJsonObject
        //é€šè¿‡æ£€æŸ¥æ˜¯å¦æœ‰specè¿™ä¸ªé”®ï¼Œæ¥åˆ¤æ–­æ˜¯å¦ä¸ºæ–°ç‰ˆæœ¬çš„Installer
        if (jsonObject.has("spec")) { //æ–°ç‰ˆå®‰è£…å™¨
            if (!jsonObject.has("version")) throw IOException("Unable to find version key!")
            //install_profile.jsonä¸­ï¼ŒæŠŠversionè¿™ä¸ªå€¼æ”¹ä¸ºcustomNameï¼Œä¹Ÿå°±å®Œæˆè‡ªå®šä¹‰ç‰ˆæœ¬åçš„æ•ˆæžœ
            jsonObject.addProperty("version", customName)
        } else { //æ—§ç‰ˆå®‰è£…å™¨
            if (!jsonObject.has("install")) throw IOException("Unable to find install key!")
            val install = jsonObject.get("install").asJsonObject
            if (!install.has("target")) throw IOException("Unable to find install-target key!")
            //æŠŠtargetè¿™ä¸ªå€¼æ”¹ä¸ºcustomNameï¼Œä¹Ÿå°±å®Œæˆæ—§ç‰ˆè‡ªå®šä¹‰ç‰ˆæœ¬åçš„æ•ˆæžœ
            install.addProperty("target", customName)
            jsonObject.add("install", install)
        }
        profileJson.writeText(jsonObject.toString())
    }

    @Throws(Throwable::class)
    private fun writeTempJarFile(jarFile: File, tempJarFile: File, profileJson: File) {
        //ä»…è·³è¿‡META-INFä¸­åŽç¼€ä¸º.SFæˆ–.RSAçš„æ–‡ä»¶ï¼Œé¿å…éªŒè¯çš„æ—¶å€™å‘çŽ°install_profile.jsonè¢«ä¿®æ”¹
        fun needSkip(entryName: String) = entryName.startsWith("META-INF/") && (entryName.endsWith(".SF") || entryName.endsWith(".RSA"))

        ZipFile(jarFile).use { zipFile ->
            ZipOutputStream(tempJarFile.outputStream()).use { zos ->
                zipFile.entries().asSequence().forEach { originalEntry ->
                    zos.putNextEntry(ZipEntry(originalEntry.name))
                    if (originalEntry.name == "install_profile.json") {
                        profileJson.inputStream().use { fis -> fis.copyTo(zos) }
                    } else {
                        if (!originalEntry.isDirectory && !needSkip(originalEntry.name)) {
                            //å†™å…¥åŽŸå§‹æ–‡ä»¶
                            zipFile.getInputStream(originalEntry).use { it.copyTo(zos) }
                        }
                    }
                    zos.closeEntry()
                }
            }
        }
    }
}
