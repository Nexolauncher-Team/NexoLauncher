package com.nexo.launcher.ui.subassembly.filelist

import android.graphics.drawable.Drawable
import com.nexo.launcher.utils.stringutils.SortStrings.Companion.compareChar
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.Date

class FileItemBean(
    @JvmField val name: String,
    @JvmField val date: Date?,
    @JvmField val size: Long?
) : Comparable<FileItemBean?> {
    @JvmField var image: Drawable? = null
    @JvmField var file: File? = null
    @JvmField var isHighlighted: Boolean = false
    @JvmField var isCanCheck: Boolean = true

    constructor(file: File) : this(
        file.name,
        Date(file.lastModified()),
        //æ–‡ä»¶å¤¹ç»Ÿè®¡å¤§å°éœ€è¦èŠ±è´¹çš„æ—¶é—´è¾ƒå¤šï¼Œåªå±•ç¤ºæ–‡ä»¶çš„å¤§å°å°±å¥½äº†
        if (file.isFile) FileUtils.sizeOf(file) else null
    ) {
        this.file = file
    }

    constructor(name: String, image: Drawable?) : this(name, null as Date?, null) {
        this.image = image
    }

    constructor(name: String, date: Date, image: Drawable?) : this(name, date, null as Long?) {
        this.image = image
    }

    override fun compareTo(other: FileItemBean?): Int {
        other ?: run { throw NullPointerException("Cannot compare to null.") }

        val thisName = file?.name ?: name
        val otherName = other.file?.name ?: other.name

        //é¦–å…ˆæ£€æŸ¥æ–‡ä»¶æ˜¯å¦ä¸ºç›®å½•
        if (this.file != null && file!!.isDirectory) {
            if (other.file != null && !other.file!!.isDirectory) {
                //ç›®å½•æŽ’åœ¨æ–‡ä»¶å‰é¢
                return -1
            }
        } else if (other.file != null && other.file!!.isDirectory) {
            //æ–‡ä»¶æŽ’åœ¨ç›®å½•åŽé¢
            return 1
        }

        return compareChar(thisName, otherName)
    }

    override fun toString(): String {
        return "FileItemBean{" +
                "file=" + file +
                ", name='" + name + '\'' +
                '}'
    }
}

