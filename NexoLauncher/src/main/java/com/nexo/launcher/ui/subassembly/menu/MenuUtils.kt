package com.nexo.launcher.ui.subassembly.menu

import android.annotation.SuppressLint
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView

class MenuUtils {
    companion object {
        /**
         * è°ƒæ•´æ»‘åŠ¨æ¡çš„å€¼
         * @param seekBar æ»‘åŠ¨æ¡
         * @param v éœ€è¦è°ƒæ•´çš„å€¼çš„å¤§å°
         */
        @JvmStatic
        fun adjustSeekbar(seekBar: SeekBar, v: Int) {
            seekBar.progress += v
        }

        /**
         * åè½¬Switchå½“å‰çš„é€‰ä¸­çŠ¶æ€
         */
        @JvmStatic
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        fun toggleSwitchState(switchView: Switch) {
            switchView.isChecked = !switchView.isChecked
        }

        /**
         * åˆå§‹åŒ–Seekbarçš„å€¼
         */
        @JvmStatic
        fun initSeekBarValue(seek: SeekBar, value: Int, valueView: TextView, suffix: String) {
            seek.progress = value
            updateSeekbarValue(value, valueView, suffix)
        }

        /**
         * æ›´æ–°Seekbaræ—è¾¹æ•°å€¼çš„æ–‡æœ¬å€¼
         */
        @JvmStatic
        fun updateSeekbarValue(value: Int, valueView: TextView, suffix: String) {
            val valueText = "$value $suffix"
            valueView.text = valueText.trim { it <= ' ' }
        }
    }
}
