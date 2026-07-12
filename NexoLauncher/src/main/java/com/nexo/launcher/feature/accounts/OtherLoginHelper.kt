package com.nexo.launcher.feature.accounts

import android.content.Context
import com.nexo.launcher.R
import com.nexo.launcher.feature.log.Logging
import com.nexo.launcher.feature.login.AuthResult
import com.nexo.launcher.feature.login.OtherLoginApi
import com.nexo.launcher.task.Task
import com.nexo.launcher.task.TaskExecutors
import com.nexo.launcher.ui.dialog.SelectRoleDialog
import com.nexo.launcher.value.MinecraftAccount
import java.util.Objects

/**
 * å¸®åŠ©ç™»å½•å¤–ç½®è´¦å·ï¼ˆåˆ›å»ºæ–°çš„å¤–ç½®è´¦å·ã€ä»…ç™»å½•å½“å‰å¤–ç½®è´¦å·ï¼‰
 */
class OtherLoginHelper(
    private val baseUrl: String,
    private val serverName: String,
    private val email: String,
    private val password: String,
    private val listener: OnLoginListener
) {
    private fun login(context: Context, loginListener: LoginAccountListener) {
        Task.runTask {
            OtherLoginApi.setBaseUrl(baseUrl)
            OtherLoginApi.login(context, email, password,
                object : OtherLoginApi.Listener {
                    override fun onSuccess(authResult: AuthResult) {
                        if (!Objects.isNull(authResult.selectedProfile)) {
                            loginListener.onlyOneRole(authResult)
                        } else {
                            loginListener.hasMultipleRoles(authResult)
                        }
                    }

                    override fun onFailed(error: String) {
                        TaskExecutors.runInUIThread {
                            listener.unLoading()
                            listener.onFailed(error)
                        }
                    }
                })
        }.beforeStart(TaskExecutors.getAndroidUI()) {
            listener.onLoading()
        }.onThrowable { e ->
            val message = "An exception was encountered while performing the login task."
            Logging.e("Other Login", message, e)
            TaskExecutors.runInUIThread {
                listener.onFailed(e.message ?: message)
            }
        }.execute()
    }

    /**
     * å°†è´¦å·ä¿¡æ¯å†™å…¥åˆ°è´¦å·å¯¹è±¡ä¸­ï¼ˆå•ç‹¬åŒºåˆ†å‡ºæ¥æ˜¯ä¸ºäº†é€‚é…ä»…ç™»å½•çš„æƒ…å†µï¼Œåˆ·æ–°è´¦å·ä¿¡æ¯ï¼‰
     * @param account éœ€è¦å†™å…¥çš„è´¦å·
     */
    private fun writeAccount(
        account: MinecraftAccount,
        authResult: AuthResult,
        userName: String,
        profileId: String,
        updateSkin: Boolean = true,
    ) {
        account.apply {
            this.accessToken = authResult.accessToken
            this.clientToken = authResult.clientToken
            this.otherBaseUrl = baseUrl
            this.otherAccount = email
            this.otherPassword = password
            this.accountType = serverName
            this.username = userName
            this.profileId = profileId
        }
        if (updateSkin) account.updateOtherSkin()
    }

    /**
     * é€šè¿‡è´¦å·å¯†ç ï¼Œç™»å½•ä¸€ä¸ªæ–°çš„è´¦å·
     */
    fun createNewAccount(context: Context) {
        login(context, object : LoginAccountListener {
            override fun onlyOneRole(authResult: AuthResult) {
                val profileId = authResult.selectedProfile.id
                val account: MinecraftAccount = MinecraftAccount.loadFromProfileID(profileId) ?: MinecraftAccount()
                writeAccount(account, authResult, authResult.selectedProfile.name, profileId)
                TaskExecutors.runInUIThread {
                    listener.unLoading()
                    listener.onSuccess(account)
                }
            }

            override fun hasMultipleRoles(authResult: AuthResult) {
                TaskExecutors.runInUIThread {
                    val selectRoleDialog = SelectRoleDialog(
                        context,
                        authResult.availableProfiles
                    ) { selectedProfile ->
                        val profileId = selectedProfile.id
                        val account: MinecraftAccount = MinecraftAccount.loadFromProfileID(profileId) ?: MinecraftAccount()
                        writeAccount(account, authResult, selectedProfile.name, profileId, updateSkin = false)
                        refresh(context, account)
                    }
                    listener.unLoading()
                    selectRoleDialog.show()
                }
            }
        })
    }

    /**
     * ä»…ä»…åªæ˜¯ç™»å½•å¤–ç½®è´¦å·ï¼ˆä½¿ç”¨è´¦å·å¯†ç ç™»å½•ï¼‰
     * JUST DO IT!!!
     */
    fun justLogin(context: Context, account: MinecraftAccount) {
        //æœªæ‰¾åˆ°åŒ¹é…çš„ID
        fun roleNotFound() {
            TaskExecutors.runInUIThread {
                listener.onFailed(context.getString(R.string.other_login_role_not_found))
            }
        }

        login(context, object : LoginAccountListener {
            override fun onlyOneRole(authResult: AuthResult) {
                if (authResult.selectedProfile.id != account.profileId) {
                    roleNotFound()
                    return
                }
                writeAccount(account, authResult, authResult.selectedProfile.name, authResult.selectedProfile.id)
                TaskExecutors.runInUIThread {
                    listener.unLoading()
                    listener.onSuccess(account)
                }
            }

            override fun hasMultipleRoles(authResult: AuthResult) {
                authResult.availableProfiles.forEach { profile ->
                    if (profile.id == account.profileId) {
                        //åŒ¹é…å½“å‰è´¦å·çš„IDæ—¶ï¼Œé‚£ä¹ˆè¿™ä¸ªè§’è‰²å°±æ˜¯è¿™ä¸ªè´¦å·
                        writeAccount(account, authResult, profile.name, profile.id)
                        TaskExecutors.runInUIThread {
                            listener.unLoading()
                            listener.onSuccess(account)
                        }
                        return
                    }
                }
                roleNotFound()
            }
        })
    }

    private fun refresh(context: Context, account: MinecraftAccount) {
        Task.runTask {
            OtherLoginApi.setBaseUrl(baseUrl)
            OtherLoginApi.refresh(context, account, true, object : OtherLoginApi.Listener {
                override fun onSuccess(authResult: AuthResult) {
                    account.accessToken = authResult.accessToken
                    account.updateOtherSkin()
                    TaskExecutors.runInUIThread {
                        listener.unLoading()
                        listener.onSuccess(account)
                    }
                }

                override fun onFailed(error: String) {
                    TaskExecutors.runInUIThread {
                        listener.unLoading()
                        listener.onFailed(error)
                    }
                }
            })
        }.beforeStart(TaskExecutors.getAndroidUI()) {
            listener.onLoading()
        }.onThrowable { e ->
            val message = "An exception was encountered while performing the refresh task."
            Logging.e("Other Login", message, e)
            TaskExecutors.runInUIThread {
                listener.onFailed(e.message ?: message)
            }
        }.execute()
    }

    interface OnLoginListener {
        fun onLoading()
        fun unLoading()
        fun onSuccess(account: MinecraftAccount)
        fun onFailed(error: String)
    }

    /**
     * è´¦å·æ‹¥æœ‰çš„è§’è‰²æ•°é‡ä¸åŒæ—¶ï¼Œæ‰€åšå‡ºçš„ç™»é™†å†³ç­–
     */
    private interface LoginAccountListener {
        fun onlyOneRole(authResult: AuthResult)

        fun hasMultipleRoles(authResult: AuthResult)
    }
}
