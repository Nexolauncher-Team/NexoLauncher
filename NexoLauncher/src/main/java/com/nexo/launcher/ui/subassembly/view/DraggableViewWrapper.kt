package com.nexo.launcher.ui.subassembly.view

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import com.nexo.launcher.utils.ZHTools
import kotlin.math.max
import kotlin.math.min

class DraggableViewWrapper(private val mainView: View, private val fetcher: AttributesFetcher) {
    private var lastUpdateTime: Long = 0
    private var initialX = 0f
    private var initialY = 0f
    private var touchX = 0f
    private var touchY = 0f

    @SuppressLint("ClickableViewAccessibility")
    fun init() {
        mainView.setOnTouchListener { _: View?, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (updateRateLimits()) return@setOnTouchListener false

                    initialX = fetcher.get()[0].toFloat()
                    initialY = fetcher.get()[1].toFloat()
                    touchX = event.rawX
                    touchY = event.rawY
                    return@setOnTouchListener true
                }

                MotionEvent.ACTION_MOVE -> {
                    if (updateRateLimits()) return@setOnTouchListener false

                    val x = max(fetcher.screenPixels.minX.toDouble(), min(fetcher.screenPixels.maxX.toDouble(),
                        (initialX + (event.rawX - touchX)).toDouble())
                    ).toInt()
                    val y = max(fetcher.screenPixels.minY.toDouble(), min(fetcher.screenPixels.maxY.toDouble(),
                        (initialY + (event.rawY - touchY)).toDouble())
                    ).toInt()
                    fetcher.set(x, y)
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    //é¿å…è¿‡äºŽé¢‘ç¹çš„æ›´æ–°å¯¼è‡´çš„æ€§èƒ½å¼€é”€
    private fun updateRateLimits(): Boolean {
        var limit = false
        val millis = ZHTools.getCurrentTimeMillis()
        if (millis - lastUpdateTime < 5) limit = true
        lastUpdateTime = millis
        return limit
    }

    interface AttributesFetcher {
        //èŽ·å–å¯¹åº”çš„å±å¹•çš„é«˜å®½é™åˆ¶å€¼
        val screenPixels: ScreenPixels
        fun get(): IntArray //èŽ·å–x, yå€¼
        fun set(x: Int, y: Int)
    }

    class ScreenPixels(var minX: Int, var minY: Int, var maxX: Int, var maxY: Int)
}

