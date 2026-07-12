package com.nexo.launcher.utils.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import java.io.File
import kotlin.math.min


class ImageUtils {
    companion object {
        /**
         * é€šè¿‡ BitmapFactory æ£€æŸ¥ä¸€ä¸ªæ–‡ä»¶æ˜¯å¦ä¸ºä¸€ä¸ªå›¾ç‰‡
         * @param file æ–‡ä»¶
         * @return è¿”å›žæ˜¯å¦ä¸ºå›¾ç‰‡
         */
        //ä½¿ç”¨æºä»£ç ï¼šhttps://github.com/lamba92/KImageCheck/blob/master/src/androidMain/kotlin/com/github/lamba92/utils/KImageCheck.kt#L12
        @JvmStatic
        fun isImage(file: File?): Boolean {
            file?.apply {
                if (isDirectory) return false
                runCatching {
                    val options = BitmapFactory.Options()
                    options.inJustDecodeBounds = true
                    BitmapFactory.decodeFile(path, options)
                    return options.outWidth != -1 || options.outHeight != -1
                }
            }
            return false
        }

        /**
         * é€šè¿‡è®¡ç®—å›¾ç‰‡çš„é•¿æ¬¾æ¯”ä¾‹æ¥è®¡ç®—ç¼©æ”¾åŽçš„é•¿æ¬¾æ•°æ®
         * @param imageWidth åŽŸå§‹å›¾ç‰‡çš„é•¿
         * @param imageHeight åŽŸå§‹å›¾ç‰‡çš„å®½
         * @param maxSize éœ€è¦é™åˆ¶åœ¨å¤šå¤§çš„ç©ºé—´
         * @return è¿”å›žä¸€ä¸ªç¼©æ”¾åŽçš„é•¿å®½æ•°æ®å¯¹è±¡
         */
        @JvmStatic
        fun resizeWithRatio(imageWidth: Int, imageHeight: Int, maxSize: Int): Dimension {
            val widthRatio = maxSize.toDouble() / imageWidth
            val heightRatio = maxSize.toDouble() / imageHeight

            //é€‰æ‹©è¾ƒå°çš„ç¼©æ”¾æ¯”ä¾‹ï¼Œç¡®ä¿é•¿å®½æŒ‰æ¯”ä¾‹ç¼©å°ä¸”ä¸è¶…è¿‡maxSizeé™åˆ¶
            val ratio = min(widthRatio, heightRatio)
            val newWidth = (imageWidth * ratio).toInt()
            val newHeight = (imageHeight * ratio).toInt()

            return Dimension(newWidth, newHeight)
        }

        /**
         * ä»Žä¸€ä¸ª ImageView ä¸­èŽ·å– Drawableï¼Œå¹¶å°†å…¶è½¬æ¢ä¸º Bitmap
         */
        @JvmStatic
        fun getBitmapFromImageView(imageView: ImageView): Bitmap? {
            val drawable = imageView.drawable ?: return null

            if (drawable is BitmapDrawable) {
                return drawable.bitmap
            }

            val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 1
            val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 1

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            drawable.mutate().setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            return bitmap
        }
    }
}

