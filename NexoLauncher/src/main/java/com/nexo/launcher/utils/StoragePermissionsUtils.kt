package com.nexo.launcher.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.nexo.launcher.InfoCenter
import com.nexo.launcher.R
import com.nexo.launcher.ui.dialog.TipDialog

class StoragePermissionsUtils {
    companion object {
        private const val REQUEST_CODE_PERMISSIONS: Int = 0
        @JvmStatic
        private var hasStoragePermission: Boolean = false

        /**
         * æ£€æŸ¥å­˜å‚¨æƒé™ï¼Œè¿”å›žæ˜¯å¦æ‹¥æœ‰å­˜å‚¨æƒé™
         */
        @JvmStatic
        fun checkPermissions(context: Context) {
            hasStoragePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                checkPermissionsForAndroid11AndAbove()
            } else {
                hasStoragePermissions(context)
            }
        }

        /**
         * èŽ·å¾—æå‰æ£€æŸ¥å¥½çš„å­˜å‚¨æƒé™
         */
        @JvmStatic
        fun checkPermissions() = hasStoragePermission

        /**
         * æ£€æŸ¥å­˜å‚¨æƒé™ï¼Œå¦‚æžœæ²¡æœ‰å­˜å‚¨æƒé™ï¼Œåˆ™å¼¹å‡ºå¼¹çª—å‘ç”¨æˆ·ç”³è¯·
         */
        @JvmStatic
        fun checkPermissions(
            activity: Activity,
            title: Int = R.string.generic_warning,
            message: String = getDefaultPermissionMessage(activity),
            permissionGranted: PermissionGranted?
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                handlePermissionsForAndroid11AndAbove(activity, title, message, permissionGranted)
            } else {
                handlePermissionsForAndroid10AndBelow(activity, title, message, permissionGranted)
            }
        }

        /**
         * é€‚ç”¨äºŽå®‰å“10åŠä¸€ä¸‹çš„å­˜å‚¨æƒé™æ£€æŸ¥
         */
        fun hasStoragePermissions(context: Context): Boolean {
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }

        @RequiresApi(api = Build.VERSION_CODES.R)
        private fun checkPermissionsForAndroid11AndAbove() = Environment.isExternalStorageManager()

        @RequiresApi(api = Build.VERSION_CODES.R)
        private fun handlePermissionsForAndroid11AndAbove(activity: Activity, title: Int, message: String, permissionGranted: PermissionGranted?) {
            if (!checkPermissionsForAndroid11AndAbove()) {
                showPermissionRequestDialog(activity, title, message, object : RequestPermissions {
                    override fun onRequest() {
                        val intent =
                            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                        intent.setData(Uri.parse("package:" + activity.packageName))
                        activity.startActivityForResult(intent, REQUEST_CODE_PERMISSIONS)
                    }

                    override fun onCancel() {
                        permissionGranted?.cancelled()
                    }
                })
            } else {
                permissionGranted?.granted()
            }
        }

        private fun handlePermissionsForAndroid10AndBelow(activity: Activity, title: Int, message: String, permissionGranted: PermissionGranted?) {
            if (!hasStoragePermissions(activity)) {
                showPermissionRequestDialog(activity, title, message, object : RequestPermissions {
                    override fun onRequest() {
                        ActivityCompat.requestPermissions(
                            activity, arrayOf(
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ), REQUEST_CODE_PERMISSIONS
                        )
                    }

                    override fun onCancel() {
                        permissionGranted?.cancelled()
                    }
                })
            } else {
                permissionGranted?.granted()
            }
        }

        private fun showPermissionRequestDialog(
            context: Context,
            title: Int,
            message: String,
            requestPermissions: RequestPermissions
        ) {
            TipDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setConfirmClickListener { requestPermissions.onRequest() }
                .setCancelClickListener { requestPermissions.onCancel() }
                .setCancelable(false)
                .showDialog()
        }

        private fun getDefaultPermissionMessage(context: Context) =
            InfoCenter.replaceName(context, R.string.permissions_manage_external_storage)
    }

    private interface RequestPermissions {
        fun onRequest()
        fun onCancel()
    }

    interface PermissionGranted {
        fun granted()
        fun cancelled()
    }
}
