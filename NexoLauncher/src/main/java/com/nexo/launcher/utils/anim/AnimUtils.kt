package com.nexo.launcher.utils.anim

import android.view.View
import com.nexo.launcher.anim.animations.Animations
import com.nexo.launcher.setting.AllSettings
import com.nexo.launcher.task.Task
import com.nexo.launcher.utils.anim.ViewAnimUtils.Companion.setViewAnim

class AnimUtils {
    companion object {
        @JvmStatic
        fun setVisibilityAnim(view: View, shouldShow: Boolean) {
            setVisibilityAnim(view, shouldShow, 300, null)
        }

        @JvmStatic
        fun setVisibilityAnim(view: View, shouldShow: Boolean, listener: AnimationListener?) {
            setVisibilityAnim(view, shouldShow, 300, listener)
        }

        @JvmStatic
        fun setVisibilityAnim(view: View, shouldShow: Boolean, duration: Int) {
            setVisibilityAnim(view, shouldShow, duration, null)
        }

        @JvmStatic
        fun setVisibilityAnim(
            view: View,
            shouldShow: Boolean,
            duration: Int,
            listener: AnimationListener?
        ) {
            setVisibilityAnim(view, 0, shouldShow, duration, listener)
        }

        @JvmStatic
        fun playVisibilityAnim(view: View, visible: Boolean) {
            val targetVisibility = if (visible) View.VISIBLE else View.GONE
            if (view.visibility == targetVisibility) return

            setViewAnim(view, if (visible) Animations.FadeIn else Animations.FadeOut,
                (AllSettings.animationSpeed.getValue() * 0.7).toLong(),
                { view.visibility = View.VISIBLE },
                { view.visibility = if (visible) View.VISIBLE else View.GONE })
        }

        /**
         * ç”¨äºŽä¾¿æ·åœ°ä½¿ç”¨éšè—åŠ¨ç”»
         * @param view éœ€è¦æ“ä½œçš„æŽ§ä»¶
         * @param startDelay å¼€å§‹å‰çš„å»¶è¿Ÿ
         * @param shouldShow true: æ˜¾ç¤ºï¼Œfalse: éšè—
         * @param duration æŒç»­æ—¶é—´
         * @param listener åŠ¨ç”»ç›‘å¬å™¨ï¼Œç”¨äºŽè°ƒç”¨åŠ¨ç”»å¼€å§‹å‰å’Œç»“æŸçš„å›žè°ƒ
         */
        @JvmStatic
        fun setVisibilityAnim(
            view: View,
            startDelay: Int,
            shouldShow: Boolean,
            duration: Int,
            listener: AnimationListener?
        ) {
            listener?.onStart()

            if (shouldShow && view.visibility != View.VISIBLE) {
                fadeAnim(view, startDelay.toLong(), 0f, 1f, duration) {
                    view.visibility = View.VISIBLE
                    listener?.onEnd()
                }
            } else if (!shouldShow && view.visibility != View.GONE) {
                fadeAnim(view, startDelay.toLong(), view.alpha, 0f, duration) {
                    view.visibility = View.GONE
                    listener?.onEnd()
                }
            }
        }

        /**
         * ç”¨äºŽä¾¿æ·åœ°ä½¿ç”¨æ¸éšæ¸æ˜¾åŠ¨ç”»
         * @param view éœ€è¦æ“ä½œçš„æŽ§ä»¶
         * @param startDelay å¼€å§‹å‰çš„å»¶è¿Ÿ
         * @param begin å¼€å§‹çš„é€æ˜Žåº¦ï¼ˆAlphaï¼‰
         * @param end ç»“æŸçš„é€æ˜Žåº¦ï¼ˆAlphaï¼‰
         * @param duration æŒç»­æ—¶é—´
         * @param endAction åŠ¨ç”»ç»“æŸæ—¶æ‰§è¡Œçš„ä»»åŠ¡
         */
        @JvmStatic
        fun fadeAnim(
            view: View,
            startDelay: Long,
            begin: Float,
            end: Float,
            duration: Int,
            endAction: Runnable?
        ) {
            if ((view.visibility != View.VISIBLE && end == 0f) || (view.visibility == View.VISIBLE && end == 1f)) {
                endAction?.let { r -> Task.runTask { r.run() }.execute() }
                return
            }
            view.visibility = View.VISIBLE
            view.alpha = begin
            view.animate()
                .alpha(end)
                .setStartDelay(startDelay)
                .setDuration(duration.toLong())
                .withEndAction(endAction)
        }
    }

    interface AnimationListener {
        fun onStart()
        fun onEnd()
    }
}

