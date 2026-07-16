package com.nexo.launcher.ui.dialog

import android.content.Context
import android.os.Bundle
import android.view.View
import com.nexo.launcher.R
import com.nexo.launcher.databinding.DialogTipBinding
import com.nexo.launcher.feature.renderer.MobileGluesHub
import com.nexo.launcher.feature.renderer.MobileGluesUpdateManager
import com.nexo.launcher.task.TaskExecutors

/**
 * A dialog to handle the automated setup of MobileGlues.
 */
class MobileGluesSetupDialog(
    context: Context,
    private val release: MobileGluesUpdateManager.ReleaseInfo,
    private val onComplete: (Boolean) -> Unit
) : FullScreenDialog(context), DraggableDialog.DialogInitializationListener {

    private val binding = DialogTipBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        DraggableDialog.initDialog(this)

        binding.apply {
            titleView.text = "MobileGlues Setup"
            messageView.text = "Preparing to download high-performance renderer (${release.version})..."
            
            cancelButton.visibility = View.VISIBLE
            cancelButton.text = "Cancel"
            cancelButton.setOnClickListener { dismiss() }

            confirmButton.visibility = View.GONE // Hide until complete or start
        }

        startDownload()
    }

    override fun onInit(): android.view.Window? = window

    private fun startDownload() {
        val downloadUrl = release.downloadUrl ?: run {
            updateUI(MobileGluesHub.SetupStatus(error = "No download URL found in release info."))
            return
        }
        
        Thread {
            MobileGluesHub.install(context, downloadUrl) { status ->
                TaskExecutors.runInUIThread {
                    updateUI(status)
                }
            }
        }.start()
    }

    private fun updateUI(status: MobileGluesHub.SetupStatus) {
        binding.apply {
            if (status.isDownloading) {
                messageView.text = "Downloading renderer: ${status.progress}%"
                // Tip: In a real project we'd add a ProgressBar to dialog_tip or use a specialized layout
            }

            if (status.error != null) {
                messageView.text = "Error: ${status.error}"
                confirmButton.visibility = View.VISIBLE
                confirmButton.text = "Retry"
                confirmButton.setOnClickListener { startDownload() }
            }

            if (status.isComplete) {
                messageView.text = "Installation complete! NexoLauncher is now ready with MobileGlues."
                confirmButton.visibility = View.VISIBLE
                confirmButton.text = "Finish"
                confirmButton.setOnClickListener {
                    onComplete(true)
                    dismiss()
                }
            }
        }
    }
}
