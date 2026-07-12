package com.nexo.launcher.firebase

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nexo.launcher.R
import com.nexo.launcher.event.single.MCOptionChangeEvent
import com.nexo.launcher.event.single.SettingsChangeEvent
import com.nexo.launcher.feature.log.Logging
import com.nexo.launcher.setting.Settings
import com.nexo.launcher.utils.path.PathManager
import com.nexo.launcher.Tools
import com.nexo.launcher.feature.MCOptions
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.File

class FirebaseBackupManager private constructor(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val gson = Gson()

    companion object {
        private var instance: FirebaseBackupManager? = null

        fun init(context: Context) {
            if (instance == null) {
                instance = FirebaseBackupManager(context.applicationContext)
                EventBus.getDefault().register(instance)
            }
        }

        fun getInstance(): FirebaseBackupManager {
            return instance ?: throw IllegalStateException("FirebaseBackupManager not initialized")
        }
    }

    fun getSignInIntent(): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso).signInIntent
    }

    fun handleSignInResult(data: Intent?, callback: (Boolean, String?) -> Unit) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            firebaseAuthWithGoogle(account.idToken!!, callback)
        } catch (e: ApiException) {
            Logging.e("FirebaseBackup", "Google sign in failed", e)
            callback(false, e.message)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, callback: (Boolean, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    restoreAll()
                    callback(true, null)
                } else {
                    callback(false, task.exception?.message)
                }
            }
    }

    fun signOut() {
        auth.signOut()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
        GoogleSignIn.getClient(context, gso).signOut()
    }

    fun isLoggedIn(): Boolean = auth.currentUser != null

    fun getUserEmail(): String? = auth.currentUser?.email

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onSettingsChanged(event: SettingsChangeEvent) {
        backupSettings()
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    fun onMCOptionsChanged(event: MCOptionChangeEvent) {
        backupMCOptions()
    }

    fun backupAll() {
        backupSettings()
        backupMCOptions()
    }

    fun backupSettings() {
        val user = auth.currentUser ?: return
        val settingsFile = PathManager.FILE_SETTINGS
        if (!settingsFile.exists()) return

        try {
            val json = Tools.read(settingsFile)
            val settingsMap: List<Map<String, String>> = gson.fromJson(json, object : TypeToken<List<Map<String, String>>>() {}.type)
            
            val data = hashMapOf(
                "settings" to settingsMap,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("users").document(user.uid)
                .collection("backups").document("launcher_settings")
                .set(data)
                .addOnSuccessListener {
                    Logging.d("FirebaseBackup", "Settings backed up successfully")
                }
                .addOnFailureListener { e ->
                    Logging.e("FirebaseBackup", "Failed to backup settings", e)
                }
        } catch (e: Exception) {
            Logging.e("FirebaseBackup", "Error reading settings file for backup", e)
        }
    }

    fun backupMCOptions() {
        val user = auth.currentUser ?: return
        val optionsFile = MCOptions.getOptionsFile()
        if (!optionsFile.exists()) return

        try {
            val content = Tools.read(optionsFile)
            val data = hashMapOf(
                "content" to content,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("users").document(user.uid)
                .collection("backups").document("mc_options")
                .set(data)
                .addOnSuccessListener {
                    Logging.d("FirebaseBackup", "MC options backed up successfully")
                }
                .addOnFailureListener { e ->
                    Logging.e("FirebaseBackup", "Failed to backup MC options", e)
                }
        } catch (e: Exception) {
            Logging.e("FirebaseBackup", "Error reading MC options for backup", e)
        }
    }

    fun restoreAll() {
        restoreSettings()
        restoreMCOptions()
    }

    fun restoreSettings() {
        val user = auth.currentUser ?: return

        db.collection("users").document(user.uid)
            .collection("backups").document("launcher_settings")
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val settingsList = document.get("settings") as? List<Map<String, String>>
                    if (settingsList != null) {
                        try {
                            val json = gson.toJson(settingsList)
                            val settingsFile = PathManager.FILE_SETTINGS
                            org.apache.commons.io.FileUtils.write(settingsFile, json, Charsets.UTF_8)
                            Settings.refreshSettings()
                            Logging.d("FirebaseBackup", "Settings restored successfully")
                        } catch (e: Exception) {
                            Logging.e("FirebaseBackup", "Failed to write restored settings", e)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Logging.e("FirebaseBackup", "Failed to restore settings", e)
            }
    }

    fun restoreMCOptions() {
        val user = auth.currentUser ?: return

        db.collection("users").document(user.uid)
            .collection("backups").document("mc_options")
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val content = document.getString("content")
                    if (content != null) {
                        try {
                            val optionsFile = MCOptions.getOptionsFile()
                            org.apache.commons.io.FileUtils.write(optionsFile, content, Charsets.UTF_8)
                            MCOptions.load()
                            Logging.d("FirebaseBackup", "MC options restored successfully")
                        } catch (e: Exception) {
                            Logging.e("FirebaseBackup", "Failed to restore MC options", e)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Logging.e("FirebaseBackup", "Failed to restore MC options", e)
            }
    }
}
