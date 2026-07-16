package com.nexo.launcher.feature.gpu

import android.content.Context
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.GLES20
import com.nexo.launcher.feature.log.Logging

/**
 * Manages GPU detection and renderer recommendations based on hardware.
 */
object GPUManager {
    private var gpuVendor: String? = null
    private var gpuRenderer: String? = null

    /**
     * Detects the GPU vendor and renderer by creating a temporary EGL context.
     * This is required because GL strings are only available with an active context.
     */
    fun detectGPU(context: Context) {
        if (gpuVendor != null) return

        try {
            val display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
            val version = IntArray(2)
            EGL14.eglInitialize(display, version, 0, version, 1)

            val attribList = intArrayOf(
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_NONE
            )
            val configs = arrayOfNulls<EGLConfig>(1)
            val numConfigs = IntArray(1)
            EGL14.eglChooseConfig(display, attribList, 0, configs, 0, 1, numConfigs, 0)

            val config = configs[0]
            val contextAttribs = intArrayOf(
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
            )
            val eglContext = EGL14.eglCreateContext(display, config, EGL14.EGL_NO_CONTEXT, contextAttribs, 0)

            val surfaceAttribs = intArrayOf(
                EGL14.EGL_WIDTH, 1,
                EGL14.EGL_HEIGHT, 1,
                EGL14.EGL_NONE
            )
            val eglSurface = EGL14.eglCreatePbufferSurface(display, config, surfaceAttribs, 0)

            EGL14.eglMakeCurrent(display, eglSurface, eglSurface, eglContext)

            gpuVendor = GLES20.glGetString(GLES20.GL_VENDOR)
            gpuRenderer = GLES20.glGetString(GLES20.GL_RENDERER)

            Logging.i("GPUManager", "Detected GPU: $gpuVendor - $gpuRenderer")

            // Cleanup
            EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
            EGL14.eglDestroySurface(display, eglSurface)
            EGL14.eglDestroyContext(display, eglContext)
            EGL14.eglTerminate(display)
        } catch (e: Exception) {
            Logging.e("GPUManager", "Failed to detect GPU details proactively", e)
        }
    }

    fun getGPUVendor(): String = gpuVendor ?: "Unknown"
    fun getGPURenderer(): String = gpuRenderer ?: "Unknown"

    fun isAdreno(): Boolean = getGPURenderer().contains("Adreno", true)
    fun isMali(): Boolean = getGPURenderer().contains("Mali", true)
    fun isPowerVR(): Boolean = getGPURenderer().contains("PowerVR", true) || getGPURenderer().contains("SGX", true)
    fun isVivante(): Boolean = getGPURenderer().contains("Vivante", true)

    /**
     * Recommends the best renderer based on the detected hardware.
     */
    fun getRecommendedRendererId(): String {
        return when {
            isAdreno() -> "vulkan_zink" // Adreno performs best with Vulkan Zink
            isMali() -> "opengles2" // GL4ES is safer/more stable on Mali drivers
            else -> "opengles2" // Safe default for compatibility
        }
    }
}
