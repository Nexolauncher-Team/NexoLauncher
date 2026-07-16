package com.nexo.launcher.plugins.renderer

import android.content.Context
import android.content.pm.ApplicationInfo
import com.nexo.launcher.R
import com.nexo.launcher.feature.log.Logging
import com.nexo.launcher.feature.update.UpdateUtils
import com.nexo.launcher.renderer.Renderers
import com.nexo.launcher.utils.path.PathManager
import com.nexo.launcher.utils.stringutils.StringUtilsKt
import com.nexo.launcher.Architecture
import com.nexo.launcher.Tools
import com.nexo.launcher.utils.ZipUtils
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipFile

/**
 * FCLã€NexoLauncher æ¸²æŸ“å™¨æ’ä»¶ï¼ŒåŒæ—¶æ”¯æŒä½¿ç”¨æœ¬åœ°æ¸²æŸ“å™¨æ’ä»¶
 * [FCL Renderer Plugin](https://github.com/FCL-Team/FCLRendererPlugin)
 */
object RendererPluginManager {
    private val rendererPluginList: MutableList<RendererPlugin> = mutableListOf()
    private val apkRendererPluginList: MutableList<ApkRendererPlugin> = mutableListOf()
    private val localRendererPluginList: MutableList<LocalRendererPlugin> = mutableListOf()

    /**
     * èŽ·å–å½“å‰æ¸²æŸ“å™¨æ’ä»¶åŠ è½½çš„æ‰€æœ‰æ¸²æŸ“å™¨
     */
    @JvmStatic
    fun getRendererList() = rendererPluginList

    /**
     * ç§»é™¤æŸäº›å·²åŠ è½½çš„æ¸²æŸ“å™¨
     */
    @JvmStatic
    fun removeRenderer(rendererPlugins: Collection<RendererPlugin>) {
        rendererPluginList.removeAll(rendererPlugins)
    }

    /**
     * èŽ·å–å½“å‰æœ¬åœ°æ¸²æŸ“å™¨æ’ä»¶åŠ è½½çš„æ‰€æœ‰æ¸²æŸ“å™¨
     */
    @JvmStatic
    fun getAllLocalRendererList() = localRendererPluginList

    /**
     * @return æ˜¯å¯ç”¨çš„
     */
    @JvmStatic
    fun isAvailable(): Boolean {
        return rendererPluginList.isNotEmpty()
    }

    /**
     * å½“å‰é€‰æ‹©çš„æ¸²æŸ“å™¨æ’ä»¶æ‰€åŠ è½½çš„æ¸²æŸ“å™¨
     * æ ¹æ®æ€»æ¸²æŸ“å™¨ç®¡ç†è€…é€‰æ‹©çš„æ¸²æŸ“å™¨çš„æ¸²æŸ“å™¨å”¯ä¸€æ ‡è¯†ç¬¦è¿›è¡Œåˆ¤æ–­
     */
    @JvmStatic
    val selectedRendererPlugin: RendererPlugin?
        get() {
            val currentRenderer = runCatching {
                Renderers.getCurrentRenderer().getUniqueIdentifier()
            }.getOrNull()
            return rendererPluginList.find { it.uniqueIdentifier == currentRenderer }
        }

    /**
     * æ¸…é™¤æ¸²æŸ“å™¨æ’ä»¶
     */
    fun clearPlugin() {
        rendererPluginList.clear()
        apkRendererPluginList.clear()
        localRendererPluginList.clear()
    }

    /**
     * å½“å‰æ¸²æŸ“å™¨æ’ä»¶æ˜¯å¦å¸¦æœ‰é…ç½®é¡¹ï¼ˆè½¯ä»¶å¼æ’ä»¶ã€ç™½åå•åŒ…åï¼‰
     */
    @JvmStatic
    fun getConfigurablePluginOrNull(rendererUniqueIdentifier: String): RendererPlugin? {
        val renderer = apkRendererPluginList.find { it.uniqueIdentifier == rendererUniqueIdentifier }
        return renderer?.takeIf { it.packageName in setOf(
                "com.bzlzhh.plugin.ngg",
                "com.bzlzhh.plugin.ngg.angleless"
            ) }
    }

    /**
     * è§£æž NexoLauncherã€FCL æ¸²æŸ“å™¨æ’ä»¶
     */
    fun parseApkPlugin(context: Context, info: ApplicationInfo) {
        if (info.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
            val metaData = info.metaData ?: return
            if (
                metaData.getBoolean("fclPlugin", false) ||
                metaData.getBoolean("nexoRendererPlugin", false)
            ) {
                val rendererString = metaData.getString("renderer") ?: return
                val des = metaData.getString("des") ?: return
                val pojavEnvString = metaData.getString("pojavEnv") ?: return
                val nativeLibraryDir = info.nativeLibraryDir
                val renderer = rendererString.split(":")

                var rendererId: String = renderer[0]
                val envList = mutableMapOf<String, String>()
                val dlopenList = mutableListOf<String>()
                pojavEnvString.split(":").forEach { envString ->
                    if (envString.contains("=")) {
                        val stringList = envString.split("=")
                        val key = stringList[0]
                        val value = stringList[1]
                        when (key) {
                            "POJAV_RENDERER" -> rendererId = value
                            "DLOPEN" -> {
                                value.split(",").forEach { lib ->
                                    dlopenList.add(lib)
                                }
                            }
                            "LIB_MESA_NAME", "MESA_LIBRARY" -> envList[key] = "$nativeLibraryDir/$value"
                            else -> envList[key] = value
                        }
                    }
                }

                val packageName = info.packageName

                val plugin = ApkRendererPlugin(
                    rendererId,
                    "$des (${
                        context.getString(
                            R.string.setting_renderer_from_plugins,
                            runCatching {
                                context.packageManager.getApplicationLabel(info)
                            }.getOrElse {
                                context.getString(R.string.generic_unknown)
                            }
                        )
                    })",
                    packageName,
                    renderer[1],
                    renderer[2].progressEglName(nativeLibraryDir),
                    nativeLibraryDir,
                    envList,
                    dlopenList,
                    packageName
                )

                rendererPluginList.add(plugin)
                apkRendererPluginList.add(plugin)
            }
        }
    }

    /**
     * ä»Žæœ¬åœ° `/files/renderer_plugins/` ç›®å½•ä¸‹å°è¯•è§£æžæ¸²æŸ“å™¨æ’ä»¶
     * @return æ˜¯å¦æ˜¯ç¬¦åˆè¦æ±‚çš„æ’ä»¶
     *
     * æ¸²æŸ“å™¨æ–‡ä»¶å¤¹æ ¼å¼
     * renderer_plugins/
     * ----æ–‡ä»¶å¤¹åç§°/
     * --------renderer_config.json (å­˜æ”¾æ¸²æŸ“å™¨å…·ä½“ä¿¡æ¯çš„é…ç½®æ–‡ä»¶)
     * --------libs/ (æ¸²æŸ“å™¨`.so`æ–‡ä»¶çš„å­˜æ”¾ç›®å½•)
     * ------------arm64-v8a/ (arm64æž¶æž„)
     * ----------------æ¸²æŸ“å™¨åº“æ–‡ä»¶.so
     * ------------armeabi-v7a/ (arm32æž¶æž„)
     * ----------------æ¸²æŸ“å™¨åº“æ–‡ä»¶.so
     * ------------x86/ (x86æž¶æž„)
     * ----------------æ¸²æŸ“å™¨åº“æ–‡ä»¶.so
     * ------------x86_64/ (x86_64æž¶æž„)
     * ----------------æ¸²æŸ“å™¨åº“æ–‡ä»¶.so
     */
    fun parseLocalPlugin(context: Context, directory: File): Boolean {
        val archModel: String = UpdateUtils.getArchModel(Architecture.getDeviceArchitecture()) ?: return false
        val libsDirectory: File = File(directory, "libs/$archModel").takeIf { it.exists() && it.isDirectory } ?: return false
        val rendererConfigFile: File = File(directory, "config").takeIf { it.exists() && it.isFile } ?: return false
        val rendererConfig: RendererConfig = runCatching {
            Tools.GLOBAL_GSON.fromJson(readLocalRendererPluginConfig(rendererConfigFile), RendererConfig::class.java)
        }.getOrElse { e ->
            Logging.e("LocalRendererPlugin", "Failed to parse the configuration file", e)
            return false
        }
        val uniqueIdentifier = directory.name
        rendererConfig.run {
            val libPath = libsDirectory.absolutePath

            val plugin = LocalRendererPlugin(
                rendererId,
                "$rendererDisplayName (${
                    context.getString(
                        R.string.setting_renderer_from_plugins,
                        uniqueIdentifier
                    )
                })",
                uniqueIdentifier,
                glName,
                eglName.progressEglName(libPath),
                libPath,
                pojavEnv.filter { it.key != "POJAV_RENDERER" },
                dlopenList ?: emptyList(),
                directory
            )

            rendererPluginList.add(plugin)
            localRendererPluginList.add(plugin)
        }
        return true
    }

    private fun String.progressEglName(libPath: String): String =
        if (startsWith("/")) "$libPath$this"
        else this

    private fun readLocalRendererPluginConfig(configFile: File): String {
        return FileInputStream(configFile).use { fileInputStream ->
            DataInputStream(fileInputStream).use { dataInputStream ->
                dataInputStream.readUTF()
            }
        }
    }

    /**
     * å¯¼å…¥æœ¬åœ°æ¸²æŸ“å™¨æ’ä»¶
     */
    fun importLocalRendererPlugin(pluginFile: File): Boolean {
        if (!pluginFile.exists() || !pluginFile.isFile) {
            Logging.i("importLocalRendererPlugin", "The compressed file does not exist or is not a valid file.")
            return false
        }

        return try {
            ZipFile(pluginFile).use { pluginZip ->
                val configEntry = pluginZip.entries().asSequence().find { it.name == "config" }
                    ?: throw IllegalArgumentException("The plugin package does not meet the requirements!")

                pluginZip.getInputStream(configEntry).use { inputStream ->
                    DataInputStream(inputStream).use { dataInputStream ->
                        val configContent = dataInputStream.readUTF()
                        Tools.GLOBAL_GSON.fromJson(configContent, RendererConfig::class.java)
                    }
                }

                val pluginFolder = File(
                    PathManager.DIR_INSTALLED_RENDERER_PLUGIN,
                    StringUtilsKt.generateUniqueUUID(
                        { string ->
                            string.replace("-", "").substring(0, 8)
                        },
                        { uuid ->
                            File(PathManager.DIR_INSTALLED_RENDERER_PLUGIN, uuid).exists()
                        }
                    )
                )

                ZipUtils.zipExtract(pluginZip, "", pluginFolder)
            }
            true
        } catch (e: Exception) {
            Logging.i("importLocalRendererPlugin", "Error: ${e.message}")
            false
        }
    }
}
