package com.nexo.launcher.renderer.renderers

import android.content.Context
import com.nexo.launcher.renderer.RendererInterface
import com.nexo.launcher.feature.renderer.MobileGluesHub
import com.nexo.launcher.context.ContextExecutor

/**
 * Implementation of the MobileGlues renderer.
 * Points to the high-performance OpenGL-to-GLES translation library.
 */
class MobileGluesRenderer : RendererInterface {
    override fun getRendererId(): String = "opengles3_desktopgl_mobile_glues"

    override fun getUniqueIdentifier(): String = "f3d2a8c1-b7e4-4d92-8f1a-6c9a3b5d2e7f"

    override fun getRendererName(): String = "MobileGlues (OpenGL 4.6)"

    override fun getRendererEnv(): Lazy<Map<String, String>> = lazy {
        val context = ContextExecutor.getApplication()
        val libPath = MobileGluesHub.getLibPath(context).absolutePath
        
        mapOf(
            "POJAVEXEC_EGL" to libPath,
            "MESA_GL_VERSION_OVERRIDE" to "4.6",
            "MESA_GLSL_VERSION_OVERRIDE" to "460",
            "MG_INTERNAL_CONFIG" to "1",
            "MG_LOG_LEVEL" to "info",
            "MG_WSI_SELECTION" to "auto", 
            "MG_FORCE_WSI_PRESENT" to "1",
            "MG_CONF_DIR" to MobileGluesHub.getLibPath(context).parentFile?.parentFile?.absolutePath.toString(),
            "LIBGL_ES" to "3"
        )
    }

    override fun getDlopenLibrary(): Lazy<List<String>> = lazy { emptyList() }

    override fun getRendererLibrary(): String {
        val context = ContextExecutor.getApplication()
        return MobileGluesHub.getLibPath(context).absolutePath
    }

    override fun getRendererEGL(): String {
        val context = ContextExecutor.getApplication()
        return MobileGluesHub.getLibPath(context).absolutePath
    }
}
