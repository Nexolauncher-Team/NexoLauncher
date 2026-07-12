package com.nexo.launcher.feature.download.install

import java.io.File

fun interface OnFileDownloadedListener {
    fun onEnded(file: File)
}
