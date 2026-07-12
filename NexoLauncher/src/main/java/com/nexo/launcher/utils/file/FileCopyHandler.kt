package com.nexo.launcher.utils.file

import android.content.Context
import com.nexo.launcher.feature.log.Logging
import com.nexo.launcher.task.Task
import com.nexo.launcher.utils.file.FileTools.Companion.getFileNameWithoutExtension
import org.apache.commons.io.FileUtils
import java.io.File
import java.util.concurrent.atomic.AtomicLong

class FileCopyHandler(
    mContext: Context,
    private val mPasteType: PasteFile.PasteType,
    private val mSelectedFiles: List<File>,
    private val mRoot: File,
    private val mTarget: File,
    private val mFileExtensionGetter: FileExtensionGetter?,
    private val endTask: Task<*>
) : FileHandler(mContext), FileSearchProgress {
    private val foundFiles = mutableMapOf<File, File>()
    private val totalFileSize = AtomicLong(0)
    private val fileSize = AtomicLong(0)
    private val fileCount = AtomicLong(0)

    fun start() {
        super.start(this)
    }

    private fun addFile(file: File) {
        fileCount.incrementAndGet()
        fileSize.addAndGet(FileUtils.sizeOf(file))
        //å½“å‰æ–‡ä»¶ - ç›®æ ‡æ–‡ä»¶
        foundFiles [file] = getNewDestination(file, getTargetFile(file), mFileExtensionGetter?.onGet(file))
    }

    private fun addDirectory(directory: File) {
        if (directory.isFile) {
            addFile(directory)
        } else {
            directory.listFiles()?.let { files ->
                if (files.isEmpty()) {
                    addFile(directory)
                } else {
                    files.forEach { file ->
                        if (file.isFile) addFile(file)
                        else if (file.isDirectory) addDirectory(file)
                    }
                }
            }
        }
    }

    private fun getTargetFile(file: File): File {
        return File(file.absolutePath.replace(mRoot.absolutePath, mTarget.absolutePath).removeSuffix(file.name))
    }

    //å¦‚æžœç›®æ ‡åœ°ç‚¹å·²å­˜åœ¨åŒåæ–‡ä»¶ï¼Œå°±å°†ç›®æ ‡æ–‡ä»¶çš„æ–‡ä»¶ååŠ ä¸Šæ•°å­—æ ‡è¯†ï¼Œé˜²æ­¢æ–‡ä»¶è¢«è¦†ç›–
    private fun getNewDestination(sourceFile: File, targetDir: File, fileExtension: String?): File {
        var extension: String? = fileExtension
        var destFile = File(targetDir, sourceFile.name)
        if (destFile.exists()) {
            val fileNameWithoutExt = getFileNameWithoutExtension(sourceFile.name, extension)
            extension ?: run {
                val dotIndex = sourceFile.name.lastIndexOf('.')
                extension = if (dotIndex == -1) "" else sourceFile.name.substring(dotIndex)
            }
            var proposedFileName: String
            var counter = 1
            while (destFile.exists()) {
                proposedFileName = "$fileNameWithoutExt ($counter)$extension"
                destFile = File(targetDir, proposedFileName)
                counter++
            }
        }
        return destFile
    }

    override fun searchFilesToProcess() {
        mSelectedFiles.forEach {
            currentTask?.let { task -> if (task.isCancelled) return@forEach }

            if (it.isFile) addFile(it)
            else if (it.isDirectory) addDirectory(it)
        }
        currentTask?.let { task -> if (task.isCancelled) return }
        totalFileSize.set(fileSize.get())
    }

    override fun processFile() {
        Logging.i("FileCopyHandler", "Copy files (total files: $fileCount, to ${mTarget.absolutePath})")
        foundFiles.entries.parallelStream().forEach { (currentFile, targetFile) ->
            currentTask?.let { task -> if (task.isCancelled) return@forEach }

            fileSize.addAndGet(-FileUtils.sizeOf(currentFile))
            fileCount.decrementAndGet()
            targetFile.parentFile?.takeIf { !it.exists() }?.mkdirs()
            when (mPasteType) {
                PasteFile.PasteType.COPY -> FileTools.copyFile(currentFile, targetFile)
                else -> FileTools.moveFile(currentFile, targetFile)
            }
        }
        currentTask?.let { task -> if (task.isCancelled) return }
        if (mPasteType == PasteFile.PasteType.MOVE) mSelectedFiles.forEach { FileUtils.deleteQuietly(it) }
    }

    override fun getCurrentFileCount() = fileCount.get()

    override fun getTotalSize() = totalFileSize.get()

    override fun getPendingSize() = fileSize.get()

    override fun onEnd() {
        endTask.execute()
    }

    interface FileExtensionGetter {
        fun onGet(file: File?): String?
    }
}

