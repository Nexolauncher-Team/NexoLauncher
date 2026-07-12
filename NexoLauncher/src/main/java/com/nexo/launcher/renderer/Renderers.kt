package com.nexo.launcher.renderer

import android.content.Context
import com.nexo.launcher.feature.log.Logging
import com.nexo.launcher.renderer.renderers.FreedrenoRenderer
import com.nexo.launcher.renderer.renderers.GL4ESRenderer
import com.nexo.launcher.renderer.renderers.PanfrostRenderer
import com.nexo.launcher.renderer.renderers.VirGLRenderer
import com.nexo.launcher.renderer.renderers.VulkanZinkRenderer
import com.nexo.launcher.Architecture
import com.nexo.launcher.Tools

/**
 * å¯åŠ¨å™¨æ‰€æœ‰æ¸²æŸ“å™¨æ€»ç®¡ç†è€…ï¼Œå¯åŠ¨å™¨å†…ç½®çš„æ¸²æŸ“å™¨ä¸Žæ¸²æŸ“å™¨æ’ä»¶åŠ è½½çš„æ¸²æŸ“å™¨ï¼Œéƒ½ä¼šåŠ è½½åˆ°è¿™é‡Œ
 */
object Renderers {
    private val renderers: MutableList<RendererInterface> = mutableListOf()
    private var compatibleRenderers: Pair<RenderersList, MutableList<RendererInterface>>? = null
    private var currentRenderer: RendererInterface? = null
    private var isInitialized: Boolean = false

    fun init(reset: Boolean = false) {
        if (isInitialized && !reset) return
        isInitialized = true

        if (reset) {
            renderers.clear()
            compatibleRenderers = null
            currentRenderer = null
        }

        addRenderers(
            GL4ESRenderer(),
            VulkanZinkRenderer(),
            VirGLRenderer(),
            FreedrenoRenderer(),
            PanfrostRenderer()
        )
    }

    /**
     * èŽ·å–å…¼å®¹å½“å‰è®¾å¤‡çš„æ‰€æœ‰æ¸²æŸ“å™¨
     */
    fun getCompatibleRenderers(context: Context): Pair<RenderersList, List<RendererInterface>> = compatibleRenderers ?: run {
        val deviceHasVulkan = Tools.checkVulkanSupport(context.packageManager)
        // Currently, only 32-bit x86 does not have the Zink binary
        val deviceHasZinkBinary = !(Architecture.is32BitsDevice() && Architecture.isx86Device())

        val compatibleRenderers1: MutableList<RendererInterface> = mutableListOf()
        renderers.forEach { renderer ->
            if (renderer.getRendererId().contains("vulkan") && !deviceHasVulkan) return@forEach
            if (renderer.getRendererId().contains("zink") && !deviceHasZinkBinary) return@forEach
            compatibleRenderers1.add(renderer)
        }

        val rendererIdentifiers: MutableList<String> = mutableListOf()
        val rendererNames: MutableList<String> = mutableListOf()
        compatibleRenderers1.forEach { renderer ->
            rendererIdentifiers.add(renderer.getUniqueIdentifier())
            rendererNames.add(renderer.getRendererName())
        }

        val rendererPair = Pair(RenderersList(rendererIdentifiers, rendererNames), compatibleRenderers1)
        compatibleRenderers = rendererPair
        rendererPair
    }

    /**
     * åŠ å…¥ä¸€äº›æ¸²æŸ“å™¨
     */
    @JvmStatic
    fun addRenderers(vararg renderers: RendererInterface) {
        renderers.forEach { renderer ->
            addRenderer(renderer)
        }
    }

    /**
     * åŠ å…¥å•ä¸ªæ¸²æŸ“å™¨
     */
    @JvmStatic
    fun addRenderer(renderer: RendererInterface): Boolean {
        return if (this.renderers.any { it.getUniqueIdentifier() == renderer.getUniqueIdentifier() }) {
            Logging.w("Renderers", "The unique identifier of this renderer (${renderer.getRendererName()} - ${renderer.getUniqueIdentifier()}) conflicts with an already loaded renderer. " +
                    "Normally, this shouldn't happen. You deliberately caused this conflict, didn't you, user?")
            false
        } else {
            this.renderers.add(renderer)
            Logging.i("Renderers", "Renderer loaded: ${renderer.getRendererName()} (${renderer.getRendererId()} - ${renderer.getUniqueIdentifier()})")
            true
        }
    }

    /**
     * è®¾ç½®å½“å‰çš„æ¸²æŸ“å™¨
     * @param context ç”¨äºŽåˆå§‹åŒ–é€‚é…å½“å‰è®¾å¤‡çš„æ¸²æŸ“å™¨
     * @param uniqueIdentifier æ¸²æŸ“å™¨çš„å”¯ä¸€æ ‡è¯†ç¬¦ï¼Œç”¨äºŽæ‰¾åˆ°å½“å‰æƒ³è¦è®¾ç½®çš„æ¸²æŸ“å™¨
     * @param retryToFirstOnFailure å¦‚æžœæœªæ‰¾åˆ°åŒ¹é…çš„æ¸²æŸ“å™¨ï¼Œæ˜¯å¦è·³å›žæ¸²æŸ“å™¨åˆ—è¡¨çš„é¦–ä¸ªæ¸²æŸ“å™¨
     */
    fun setCurrentRenderer(context: Context, uniqueIdentifier: String, retryToFirstOnFailure: Boolean = true) {
        if (!isInitialized) throw IllegalStateException("Uninitialized renderer!")
        val compatibleRenderers = getCompatibleRenderers(context).second
        currentRenderer = compatibleRenderers.find { it.getUniqueIdentifier() == uniqueIdentifier } ?: run {
            if (retryToFirstOnFailure) {
                val renderer = compatibleRenderers[0]
                Logging.w("Renderers", "Incompatible renderer $uniqueIdentifier will be replaced with ${renderer.getUniqueIdentifier()} (${renderer.getRendererName()})")
                renderer
            } else null
        }
    }

    /**
     * èŽ·å–å½“å‰çš„æ¸²æŸ“å™¨
     */
    fun getCurrentRenderer(): RendererInterface {
        if (!isInitialized) throw IllegalStateException("Uninitialized renderer!")
        return currentRenderer ?: throw IllegalStateException("Current renderer not set")
    }

    /**
     * å½“å‰æ˜¯å¦è®¾ç½®äº†æ¸²æŸ“å™¨
     */
    fun isCurrentRendererValid(): Boolean = isInitialized && this.currentRenderer != null
}
