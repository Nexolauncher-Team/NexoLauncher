package com.nexo.launcher.task

/**
 * ä»»åŠ¡æ‰§è¡Œçš„å„ç§é˜¶æ®µçš„ç›‘å¬å™¨
 */
interface TaskExecutionPhaseListener {
    fun onBeforeStart() {}
    fun execute() {}
    fun onEnded() {}
    fun onFinally() {}
    /**
     * ä»»åŠ¡æ‰§è¡Œä¸­è§¦å‘å¼‚å¸¸åŽå°†ä¼šæ‰§è¡Œçš„å†…å®¹
     * @param throwable è§¦å‘çš„å¼‚å¸¸
     */
    fun onThrowable(throwable: Throwable) {}
}
