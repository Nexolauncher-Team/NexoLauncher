package com.nexo.launcher.renderer

/**
 * å¯åŠ¨å™¨æ¸²æŸ“å™¨å®žçŽ°
 */
interface RendererInterface {
    /**
     * èŽ·å–æ¸²æŸ“å™¨çš„ID
     */
    fun getRendererId(): String

    /**
     * èŽ·å–æ¸²æŸ“å™¨çš„å”¯ä¸€æ ‡è¯†ID
     */
    fun getUniqueIdentifier(): String

    /**
     * èŽ·å–æ¸²æŸ“å™¨çš„åç§°
     */
    fun getRendererName(): String

    /**
     * èŽ·å–æ¸²æŸ“å™¨çš„çŽ¯å¢ƒå˜é‡
     */
    fun getRendererEnv(): Lazy<Map<String, String>>

    /**
     * èŽ·å–éœ€è¦dlopençš„åº“
     */
    fun getDlopenLibrary(): Lazy<List<String>>

    /**
     * èŽ·å–æ¸²æŸ“å™¨çš„åº“
     */
    fun getRendererLibrary(): String

    /**
     * èŽ·å–EGLåç§°
     */
    fun getRendererEGL(): String? = null
}
