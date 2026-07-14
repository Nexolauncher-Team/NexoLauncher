package com.nexo.launcher.renderer.renderers

import com.nexo.launcher.renderer.RendererInterface
import kotlin.Lazy
import kotlin.collections.Map
import kotlin.collections.List
import kotlin.collections.mapOf
import kotlin.collections.emptyList
import kotlin.Pair

class MobileGlueOpenGLRenderer : RendererInterface {
    override fun getRendererId(): String = "mobile_glues"

    override fun getUniqueIdentifier(): String = "f3d2a8c1-b7e4-4d92-8f1a-6c9a3b5d2e7f"

    override fun getRendererName(): String = "MobileGlues (OpenGL 4.6, 1.17+)"

    override fun getRendererEnv(): Lazy<Map<String, String>> = lazy {
        mapOf(
            Pair("MESA_GL_VERSION_OVERRIDE", "4.6"),
            Pair("MESA_GLSL_VERSION_OVERRIDE", "460")
        )
    }

    override fun getDlopenLibrary(): Lazy<List<String>> = lazy { emptyList<String>() }

    override fun getRendererLibrary(): String = "libOSMesa_8.so"
}
