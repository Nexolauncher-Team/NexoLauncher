package com.nexo.launcher.feature.accounts

import android.content.Context
import com.nexo.launcher.mcgui.ProgressLayout
import com.nexo.launcher.R
import com.nexo.launcher.feature.log.Logging
import com.nexo.launcher.task.Task
import com.nexo.launcher.Tools
import com.nexo.launcher.authenticator.listener.DoneListener
import com.nexo.launcher.authenticator.listener.ErrorListener
import com.nexo.launcher.authenticator.microsoft.MicrosoftBackgroundLogin
import com.nexo.launcher.value.MinecraftAccount
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.Objects

class AccountUtils {
    companion object {
        @JvmStatic
        fun microsoftLogin(context: Context, account: MinecraftAccount, doneListener: DoneListener, errorListener: ErrorListener) {
            MicrosoftBackgroundLogin(true, account.msaRefreshToken)
                .performLogin(context, account, doneListener, errorListener)
        }

        @JvmStatic
        fun otherLogin(context: Context, account: MinecraftAccount, doneListener: DoneListener, errorListener: ErrorListener) {
            fun clearProgress() = ProgressLayout.clearProgress(ProgressLayout.LOGIN_ACCOUNT)

            Task.runTask {
                OtherLoginHelper(account.otherBaseUrl, account.accountType, account.otherAccount, account.otherPassword,
                    object : OtherLoginHelper.OnLoginListener {
                        override fun onLoading() {
                            ProgressLayout.setProgress(ProgressLayout.LOGIN_ACCOUNT, 0, R.string.account_login_start)
                        }

                        override fun unLoading() {}

                        override fun onSuccess(account: MinecraftAccount) {
                            account.save()
                            clearProgress()
                            doneListener.onLoginDone(account)
                        }

                        override fun onFailed(error: String) {
                            clearProgress()
                            errorListener.onLoginError(RuntimeException(error))
                            ProgressLayout.clearProgress(ProgressLayout.LOGIN_ACCOUNT)
                        }
                    }).justLogin(context, account)
            }.onThrowable { t -> errorListener.onLoginError(RuntimeException(t.message)) }.execute()
        }

        @JvmStatic
        fun isOtherLoginAccount(account: MinecraftAccount): Boolean {
            return !Objects.isNull(account.otherBaseUrl) && account.otherBaseUrl != "0"
        }

        @JvmStatic
        fun isMicrosoftAccount(account: MinecraftAccount): Boolean {
            return account.accountType == AccountType.MICROSOFT.type
        }

        @JvmStatic
        fun isNoLoginRequired(account: MinecraftAccount?): Boolean {
            return account == null || account.accountType == AccountType.LOCAL.type
        }

        @JvmStatic
        fun getAccountTypeName(context: Context, account: MinecraftAccount): String {
            return if (isMicrosoftAccount(account)) {
                context.getString(R.string.account_microsoft_account)
            } else if (isOtherLoginAccount(account)) {
                account.accountType
            } else {
                context.getString(R.string.account_local_account)
            }
        }

        /**
         * ä¿®æ”¹è‡ªæºä»£ç ï¼š[HMCL Core: AuthlibInjectorServer.java](https://github.com/HMCL-dev/HMCL/blob/main/HMCLCore/src/main/java/org/jackhuang/hmcl/auth/authlibinjector/AuthlibInjectorServer.java#L60-#L76)
         * <br>åŽŸé¡¹ç›®ç‰ˆæƒå½’åŽŸä½œè€…æ‰€æœ‰ï¼Œéµå¾ªGPL v3åè®®
         */
        fun tryGetFullServerUrl(baseUrl: String): String {
            fun String.addSlashIfMissing(): String {
                if (!endsWith("/")) return "$this/"
                return this
            }

            var url = addHttpsIfMissing(baseUrl)
            runCatching {
                var conn = URL(url).openConnection() as HttpURLConnection
                conn.getHeaderField("x-authlib-injector-api-location")?.let { ali ->
                    val absoluteAli = URL(conn.url, ali)
                    url = url.addSlashIfMissing()
                    val absoluteUrl = absoluteAli.toString().addSlashIfMissing()
                    if (url != absoluteUrl) {
                        conn.disconnect()
                        url = absoluteUrl
                        conn = absoluteAli.openConnection() as HttpURLConnection
                    }
                }

                return url.addSlashIfMissing()
            }.getOrElse { e ->
                Logging.e("getFullServerUrl", Tools.printToString(e))
            }
            return baseUrl
        }

        /**
         * ä¿®æ”¹è‡ªæºä»£ç ï¼š[HMCL Core: AuthlibInjectorServer.java](https://github.com/HMCL-dev/HMCL/blob/main/HMCLCore/src/main/java/org/jackhuang/hmcl/auth/authlibinjector/AuthlibInjectorServer.java#L90-#L96)
         * <br>åŽŸé¡¹ç›®ç‰ˆæƒå½’åŽŸä½œè€…æ‰€æœ‰ï¼Œéµå¾ªGPL v3åè®®
         */
        private fun addHttpsIfMissing(baseUrl: String): String {
            return if (!baseUrl.startsWith("http://", true) && !baseUrl.startsWith("https://")) {
                "https://$baseUrl".lowercase(Locale.ROOT)
            } else baseUrl.lowercase(Locale.ROOT)
        }
    }
}

