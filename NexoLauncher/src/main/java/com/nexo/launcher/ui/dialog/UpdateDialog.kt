package com.nexo.launcher.ui.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Toast
import com.nexo.launcher.R
import com.nexo.launcher.databinding.DialogUpdateBinding
import com.nexo.launcher.feature.update.LauncherVersion
import com.nexo.launcher.feature.update.LauncherVersion.WhatsNew
import com.nexo.launcher.feature.update.UpdateLauncher
import com.nexo.launcher.feature.update.UpdateUtils.Companion.getFileSize
import com.nexo.launcher.setting.AllSettings.Companion.ignoreUpdate
import com.nexo.launcher.task.TaskExecutors.Companion.runInUIThread
import com.nexo.launcher.ui.dialog.DraggableDialog.DialogInitializationListener
import com.nexo.launcher.utils.ZHTools
import com.nexo.launcher.utils.file.FileTools.Companion.formatFileSize
import com.nexo.launcher.utils.stringutils.StringUtils

class UpdateDialog(context: Context, private val launcherVersion: LauncherVersion) :
    FullScreenDialog(context), DialogInitializationListener {
    private val binding = DialogUpdateBinding.inflate(
        layoutInflater
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.setCancelable(false)
        this.setContentView(binding.root)

        init()
        DraggableDialog.initDialog(this)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun init() {
        binding.apply {
            getLanguageText(launcherVersion.title).takeIf { it != "NONE" }?.let { titleString ->
                titleText.visibility = View.VISIBLE
                titleText.text = titleString
            }

            versionName.text = StringUtils.insertSpace(context.getString(R.string.update_dialog_version), launcherVersion.versionName)
            updateTime.text = StringUtils.insertSpace(context.getString(R.string.update_dialog_time), StringUtils.formattingTime(launcherVersion.publishedAt))
            fileSize.text = StringUtils.insertSpace(context.getString(R.string.update_dialog_file_size), formatFileSize(getFileSize(launcherVersion.fileSize)))
            versionType.text = StringUtils.insertSpace(context.getString(R.string.about_version_status), getVersionType())

            ZHTools.getWebViewAfterProcessing(description)

            description.settings.javaScriptEnabled = true
            description.loadDataWithBaseURL(null, StringUtils.markdownToHtml(getLanguageText(launcherVersion.description)), "text/html", "UTF-8", null)

            updateButton.setOnClickListener {
                dismiss()
                runInUIThread {
                    Toast.makeText(context, context.getString(R.string.update_downloading_tip, "Github Release"), Toast.LENGTH_SHORT).show()
                }
                val updateLauncher = UpdateLauncher(context, launcherVersion)
                updateLauncher.start()
            }
            cancelButton.setOnClickListener { dismiss() }
            ignoreButton.setOnClickListener {
                ignoreUpdate.put(launcherVersion.versionName).save()
                dismiss()
            }
        }
    }

    private fun getVersionType(): String {
        return context.getString(if (launcherVersion.isPreRelease) R.string.generic_pre_release else R.string.generic_release)
    }

    private fun getLanguageText(whatsNew: WhatsNew): String {
        val text = when (ZHTools.getSystemLanguage()) {
            "zh_cn" -> whatsNew.zhCN
            "zh_tw" -> whatsNew.zhTW
            else -> whatsNew.enUS
        }
        return text
    }

    override fun onInit(): Window? {
        return window
    }
}

