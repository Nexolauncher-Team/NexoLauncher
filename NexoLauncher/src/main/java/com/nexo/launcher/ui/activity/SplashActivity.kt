package com.nexo.launcher.ui.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.nexo.launcher.InfoCenter
import com.nexo.launcher.InfoDistributor
import com.nexo.launcher.R
import com.nexo.launcher.databinding.ActivitySplashBinding
import com.nexo.launcher.feature.unpack.Components
import com.nexo.launcher.feature.unpack.Jre
import com.nexo.launcher.feature.unpack.UnpackComponentsTask
import com.nexo.launcher.feature.unpack.UnpackJreTask
import com.nexo.launcher.feature.unpack.UnpackSingleFilesTask
import com.nexo.launcher.task.Task
import com.nexo.launcher.ui.dialog.TipDialog
import com.nexo.launcher.utils.StoragePermissionsUtils
import com.nexo.launcher.LauncherActivity
import com.nexo.launcher.MissingStorageActivity
import com.nexo.launcher.Tools

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {
    private var isStarted: Boolean = false
    private lateinit var binding: ActivitySplashBinding
    private lateinit var installableAdapter: InstallableAdapter
    private val items: MutableList<InstallableItem> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initItems()

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleText.text = InfoDistributor.APP_NAME
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SplashActivity)
            adapter = installableAdapter
        }

        binding.startButton.apply {
            setOnClickListener {
                if (isStarted) return@setOnClickListener
                isStarted = true
                binding.splashText.setText(R.string.splash_screen_installing)
                installableAdapter.startAllTasks()
            }
            isClickable = false
        }

        if (!Tools.checkStorageRoot()) {
            startActivity(Intent(this, MissingStorageActivity::class.java))
            finish()
            return
        }

        //å¦‚æžœå®‰å“ç‰ˆæœ¬å°äºŽç­‰äºŽ9ï¼Œåˆ™æ£€æŸ¥å­˜å‚¨æƒé™ï¼ˆä¸æ˜¯ç®¡ç†æ‰€æœ‰æ–‡ä»¶æƒé™ï¼‰ï¼Œæ‹¥æœ‰å­˜å‚¨æƒé™ä¼šä¿è¯æ–‡ä»¶ã€æ–‡ä»¶å¤¹æ­£å¸¸åˆ›å»º
        //ä½†æ˜¯å¹¶ä¸å¼ºåˆ¶è¦æ±‚ç”¨æˆ·å¿…é¡»æŽˆäºˆæƒé™ï¼Œå¦‚æžœç”¨æˆ·æ‹’ç»ï¼Œé‚£ä¹ˆä¹‹åŽäº§ç”Ÿçš„é—®é¢˜å°†ç”±ç”¨æˆ·æ‰¿æ‹…
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && !StoragePermissionsUtils.hasStoragePermissions(this)) {
            TipDialog.Builder(this)
                .setTitle(R.string.generic_warning)
                .setMessage(InfoCenter.replaceName(this, R.string.permissions_write_external_storage))
                .setWarning()
                .setConfirmClickListener { requestStoragePermissions() }
                .setCancelClickListener { checkEnd() } //ç”¨æˆ·å–æ¶ˆï¼Œé‚£å°±è·Ÿéšç”¨æˆ·çš„æ„æ„¿
                .showDialog()
        } else {
            checkEnd()
        }
    }

    private fun requestStoragePermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            STORAGE_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            //æ— è®ºç”¨æˆ·æ˜¯å¦æŽˆäºˆäº†æƒé™ï¼Œéƒ½ä¼šå®Œæˆæ£€æŸ¥ï¼Œå› ä¸ºå¯åŠ¨å™¨å¹¶ä¸å¼ºåˆ¶è¦æ±‚æƒé™
            //ä½†æ˜¯ä¸€æ—¦å› ä¸ºå­˜å‚¨æƒé™å‡ºçŽ°äº†é—®é¢˜ï¼Œé‚£ä¹ˆå°†ç”±ç”¨æˆ·è‡ªè¡Œæ‰¿æ‹…åŽæžœ
            checkEnd()
        }
    }

    private fun initItems() {
        Components.entries.forEach {
            val unpackComponentsTask = UnpackComponentsTask(this, it)
            if (!unpackComponentsTask.isCheckFailed()) {
                items.add(
                    InstallableItem(
                        it.displayName,
                        it.summary?.let { it1 -> getString(it1) },
                        unpackComponentsTask
                    )
                )
            }
        }
        Jre.entries.forEach {
            val unpackJreTask = UnpackJreTask(this, it)
            if (!unpackJreTask.isCheckFailed()) {
                items.add(
                    InstallableItem(
                        it.jreName,
                        getString(it.summary),
                        unpackJreTask
                    )
                )
            }
        }
        items.sort()
        installableAdapter = InstallableAdapter(items) {
            toMain()
        }
    }
    
    private fun checkEnd() {
        installableAdapter.checkAllTask()
        Task.runTask {
            UnpackSingleFilesTask(this).run()
        }.execute()

        binding.startButton.isClickable = true
    }

    private fun toMain() {
        startActivity(Intent(this, LauncherActivity::class.java))
        finish()
    }

    companion object {
        private const val STORAGE_PERMISSION_REQUEST_CODE: Int = 100
    }
}
