package com.nexo.launcher.feature.notice

import com.nexo.launcher.feature.log.Logging
import com.nexo.launcher.setting.AllSettings
import com.nexo.launcher.utils.ZHTools
import com.nexo.launcher.utils.http.CallUtils
import com.nexo.launcher.utils.http.CallUtils.CallbackListener
import com.nexo.launcher.utils.path.UrlManager
import com.nexo.launcher.utils.stringutils.StringUtils
import com.nexo.launcher.Tools
import okhttp3.Call
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.Objects

class CheckNewNotice {
    companion object {
        @JvmStatic
        var noticeInfo: NoticeInfo? = null
        private var isChecking = false

        private fun checkCooling(): Boolean {
            return ZHTools.getCurrentTimeMillis() - AllSettings.noticeCheck.getValue() > 2 * 60 * 1000 //2åˆ†é’Ÿå†·å´
        }

        @JvmStatic
        fun checkNewNotice(listener: CheckNoticeListener) {
            if (isChecking) {
                return
            }
            isChecking = true

            noticeInfo?.let {
                listener.onSuccessful(noticeInfo)
                isChecking = false
                return
            }

            if (!checkCooling()) {
                return
            } else {
                AllSettings.noticeCheck.put(ZHTools.getCurrentTimeMillis()).save()
            }

            CallUtils(object : CallbackListener {
                override fun onFailure(call: Call?) {
                    isChecking = false
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call?, response: Response?) {
                    if (!response!!.isSuccessful) {
                        Logging.e("CheckNewNotice", "Unexpected code ${response.code}")
                    } else {
                        runCatching {
                            Objects.requireNonNull(response.body)
                            val responseBody = response.body!!.string()

                            val originJson = JSONObject(responseBody)
                            val rawBase64 = originJson.getString("content")
                            //base64è§£ç ï¼Œå› ä¸ºè¿™é‡Œè¯»å–çš„æ˜¯ä¸€ä¸ªç»è¿‡Base64åŠ å¯†åŽçš„æ–‡æœ¬
                            val rawJson = StringUtils.decodeBase64(rawBase64)

                            val noticeJson = Tools.GLOBAL_GSON.fromJson(rawJson, NoticeJsonObject::class.java)

                            //èŽ·å–é€šçŸ¥æ¶ˆæ¯
                            val language = ZHTools.getSystemLanguage()
                            val title = getLanguageText(language, noticeJson.title)
                            val content = getLanguageText(language, noticeJson.content)

                            noticeInfo = NoticeInfo(title, content, noticeJson.date, noticeJson.numbering)
                            listener.onSuccessful(noticeInfo)
                        }.getOrElse { e ->
                            Logging.e("Check New Notice", "Failed to resolve the notice.", e)
                        }
                    }
                    isChecking = false
                }
            }, "${UrlManager.URL_GITHUB_HOME}launcher_notice.json", null).enqueue()
        }

        private fun getLanguageText(language: String, text: NoticeJsonObject.Text): String {
            return when (language) {
                "zh_cn" -> text.zhCN
                "zh_tw" -> text.zhTW
                else -> text.enUS
            }
        }
    }
}

