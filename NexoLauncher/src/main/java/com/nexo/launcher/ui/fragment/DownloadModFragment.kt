package com.nexo.launcher.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.nexo.launcher.anim.AnimPlayer
import com.nexo.launcher.anim.animations.Animations
import com.nexo.launcher.R
import com.nexo.launcher.event.value.DownloadPageEvent
import com.nexo.launcher.feature.download.InfoViewModel
import com.nexo.launcher.feature.download.ScreenshotAdapter
import com.nexo.launcher.feature.download.VersionAdapter
import com.nexo.launcher.feature.download.enums.Classify
import com.nexo.launcher.feature.download.enums.ModLoader
import com.nexo.launcher.feature.download.item.InfoItem
import com.nexo.launcher.feature.download.item.ModVersionItem
import com.nexo.launcher.feature.download.item.ScreenshotItem
import com.nexo.launcher.feature.download.item.VersionItem
import com.nexo.launcher.feature.download.platform.AbstractPlatformHelper
import com.nexo.launcher.feature.log.Logging
import com.nexo.launcher.feature.version.VersionsManager
import com.nexo.launcher.setting.AllSettings
import com.nexo.launcher.task.Task
import com.nexo.launcher.task.TaskExecutors
import com.nexo.launcher.ui.subassembly.modlist.ModListAdapter
import com.nexo.launcher.ui.subassembly.modlist.ModListFragment
import com.nexo.launcher.ui.subassembly.modlist.ModListItemBean
import com.nexo.launcher.ui.view.AnimButton
import com.nexo.launcher.utils.MCVersionRegex.Companion.RELEASE_REGEX
import com.nexo.launcher.utils.ZHTools
import com.nexo.launcher.utils.stringutils.StringUtilsKt
import com.nexo.launcher.Tools
import org.greenrobot.eventbus.EventBus
import org.jackhuang.hmcl.ui.versions.ModTranslations
import org.jackhuang.hmcl.util.versioning.VersionNumber
import java.util.Objects
import java.util.concurrent.Future
import java.util.function.Consumer

class DownloadModFragment : ModListFragment() {
    companion object {
        const val TAG: String = "DownloadModFragment"
    }

    private lateinit var platformHelper: AbstractPlatformHelper
    private lateinit var mInfoItem: InfoItem
    private var linkGetSubmit: Future<*>? = null

    override fun init() {
        parseViewModel()
        super.init()
    }

    @SuppressLint("CheckResult")
    override fun refreshCreatedView() {
        linkGetSubmit = TaskExecutors.getDefault().submit {
            runCatching {
                val webUrl = platformHelper.getWebUrl(mInfoItem)
                fragmentActivity?.runOnUiThread { setLink(webUrl) }
            }.getOrElse { e ->
                Logging.e("DownloadModFragment", "Failed to retrieve the website link, ${Tools.printToString(e)}")
            }
        }

        mInfoItem.apply {
            val type = ModTranslations.getTranslationsByRepositoryType(classify)
            val mod = type.getModByCurseForgeId(slug)

            setTitleText(
                if (ZHTools.areaChecks("zh")) {
                    mod?.displayName ?: title
                } else title
            )
            setDescription(description)
            mod?.let {
                setMCMod(
                    StringUtilsKt.getNonEmptyOrBlank(type.getMcmodUrl(it))
                )
            }
            loadScreenshots()

            iconUrl?.apply {
                Glide.with(fragmentActivity!!).load(this).apply {
                    if (!AllSettings.resourceImageCache.getValue()) diskCacheStrategy(DiskCacheStrategy.NONE)
                }.into(getIconView())
            }
        }
    }

    override fun initRefresh(): Future<*> {
        return refresh(false)
    }

    override fun refresh(): Future<*> {
        return refresh(true)
    }

    override fun onDestroy() {
        EventBus.getDefault().post(DownloadPageEvent.RecyclerEnableEvent(true))
        linkGetSubmit?.apply {
            if (!isCancelled && !isDone) cancel(true)
        }
        super.onDestroy()
    }

    private fun refresh(force: Boolean): Future<*> {
        return TaskExecutors.getDefault().submit {
            runCatching {
                TaskExecutors.runInUIThread {
                    cancelFailedToLoad()
                    componentProcessing(true)
                }
                val versions = platformHelper.getVersions(mInfoItem, force)
                processDetails(versions)
            }.getOrElse { e ->
                TaskExecutors.runInUIThread {
                    componentProcessing(false)
                    setFailedToLoad(e.toString())
                }
                Logging.e("DownloadModFragment", Tools.printToString(e))
            }
        }
    }

    private fun processDetails(versions: List<VersionItem>?) {
        val pattern = RELEASE_REGEX

        val releaseCheckBoxChecked = releaseCheckBox.isChecked
        //åœ¨Keyå†…åŒæ—¶è®°å½•MCç‰ˆæœ¬ï¼Œä¸ŽModåŠ è½½å™¨ä¿¡æ¯ï¼Œä»¥ä¾¿ä¹‹åŽç»†åˆ†ModåŠ è½½å™¨
        val mModVersionsByMinecraftVersion: MutableMap<Pair<String, ModLoader?>, MutableList<VersionItem>> = HashMap()

        versions?.forEach(Consumer { versionItem ->
            currentTask?.apply { if (isCancelled) return@Consumer }

            for (mcVersion in versionItem.mcVersions) {
                currentTask?.apply { if (isCancelled) return@Consumer }

                if (releaseCheckBoxChecked) {
                    val matcher = pattern.matcher(mcVersion)
                    if (!matcher.matches()) {
                        //å¦‚æžœä¸æ˜¯æ­£å¼ç‰ˆæœ¬ï¼Œå°†ç»§ç»­æ£€æµ‹ä¸‹ä¸€é¡¹
                        continue
                    }
                }

                if (versionItem is ModVersionItem) {
                    val modloaders = versionItem.modloaders
                    if (modloaders.isNotEmpty()) {
                        modloaders.forEach {
                            addIfAbsent(mModVersionsByMinecraftVersion, Pair(mcVersion, it), versionItem)
                        }
                        //å½“è¿™ä¸ªç‰ˆæœ¬æ˜¯ä¸€ä¸ª ModVersionItem çš„æ—¶å€™ï¼Œåˆ™æ£€æŸ¥å…¶ModåŠ è½½å™¨æ˜¯å¦ä¸ä¸ºç©ºï¼Œå¦‚æžœä¸ä¸ºç©ºï¼Œåˆ™å°†ç‰ˆæœ¬æ”¯æŒçš„ModåŠ è½½å™¨ï¼Œæ”¾åˆ°ä¸åŒçš„ModåŠ è½½å™¨åˆ—è¡¨ä¸­
                        //è¿™æ ·ä¼šè®©ç”¨æˆ·æ›´å®¹æ˜“æ‰¾åˆ°åŒ¹é…è‡ªå·±éœ€è¦çš„ModåŠ è½½å™¨çš„ç‰ˆæœ¬
                        continue //å·²ç»åˆ†ç±»å®Œæ¯•ï¼Œæ²¡æœ‰å¿…è¦å†å°†è¿™ä¸ªç‰ˆæœ¬åŠ å…¥è¿›æ™®é€šçš„ç‰ˆæœ¬åˆ—è¡¨ä¸­äº†
                    }
                }
                addIfAbsent(mModVersionsByMinecraftVersion, Pair(mcVersion, null), versionItem)
            }
        })

        currentTask?.apply { if (isCancelled) return }

        val currentVersion = VersionsManager.getCurrentVersion()
        //å®šä½é¦–æ¬¡é€‚é…çš„ç‰ˆæœ¬ï¼Œå¹¶è®°å½•å…¶ç´¢å¼•ï¼Œåœ¨åŠ è½½å®Œæˆä¹‹åŽï¼ŒRecyclerView ä¼šæ»šåŠ¨åˆ°è¿™ä¸ªç´¢å¼•å¤„
        var firstAdaptIndex: Int? = null

        val mData: MutableList<ModListItemBean> = ArrayList()
        mModVersionsByMinecraftVersion.entries
            .sortedWith { entry1, entry2 ->
                val mcVersionComparison = -VersionNumber.compare(entry1.key.first, entry2.key.first)
                if (mcVersionComparison != 0) {
                    mcVersionComparison
                } else {
                    val name1 = entry1.key.second?.name ?: ""
                    val name2 = entry2.key.second?.name ?: ""
                    //ä¿è¯æœ‰ModLoaderçš„ç‰ˆæœ¬åœ¨å‰
                    if (name1.isEmpty() && name2.isNotEmpty()) 1
                    else if (name1.isNotEmpty() && name2.isEmpty()) -1
                    else name1.compareTo(name2)
                }
            }
            .forEachIndexed { index: Int, entry: Map.Entry<Pair<String, ModLoader?>, List<VersionItem>> ->
                currentTask?.apply { if (isCancelled) return }

                val isAdapt: Boolean = when (mInfoItem.classify) {
                    Classify.MODPACK -> false
                    else -> currentVersion?.let { version ->
                        val itemVersion = VersionNumber.asVersion(entry.key.first).canonical
                        val currentVersionString = VersionNumber.asVersion(version.getVersionInfo()?.minecraftVersion ?: "").canonical

                        if (!Objects.equals(itemVersion, currentVersionString)) return@let false

                        val modloader = entry.key.second
                        val loaderInfo = version.getVersionInfo()?.loaderInfo

                        when {
                            //èµ„æºæ²¡æœ‰æ¨¡ç»„åŠ è½½å™¨ä¿¡æ¯ï¼Œç›´æŽ¥åˆ¤å®šé€‚é…
                            modloader == null -> true
                            //èµ„æºæœ‰æ¨¡ç»„åŠ è½½å™¨ï¼Œä½†å½“å‰ç‰ˆæœ¬æ²¡æœ‰æ¨¡ç»„åŠ è½½å™¨ä¿¡æ¯ï¼Œä¸é€‚é…
                            //ï¼ˆä¸è£…æ¨¡ç»„åŠ è½½å™¨ä½ æƒ³è£…ä»€ä¹ˆæ¨¡ç»„ï¼Ÿï¼‰
                            loaderInfo == null -> false
                            //åŒ¹é…æ¨¡ç»„åŠ è½½å™¨
                            else -> loaderInfo.any { loader -> Objects.equals(modloader.loaderName, loader.name) }
                        }
                    } ?: false
                }

                if (isAdapt) {
                    firstAdaptIndex ?: run {
                        firstAdaptIndex = index
                    }
                }

                mData.add(
                    ModListItemBean(
                        entry.key.first,
                        entry.key.second,
                        isAdapt,
                        VersionAdapter(mInfoItem, platformHelper, entry.value)
                    )
                )
            }

        currentTask?.apply { if (isCancelled) return }

        Task.runTask(TaskExecutors.getAndroidUI()) {
            runCatching {
                var modAdapter = recyclerView.adapter as ModListAdapter?
                modAdapter ?: run {
                    modAdapter = ModListAdapter(this, mData)
                    recyclerView.layoutManager = LinearLayoutManager(fragmentActivity!!)
                    recyclerView.adapter = modAdapter
                    return@runCatching
                }
                modAdapter?.updateData(mData)
            }.getOrElse { e ->
                Logging.e("Set Adapter", Tools.printToString(e))
            }

            componentProcessing(false)
            recyclerView.scheduleLayoutAnimation()

            firstAdaptIndex?.let {
                recyclerView.postDelayed(
                    {
                        //ç›´æŽ¥æ»šåŠ¨åˆ°å…ˆå‰èŽ·å–åˆ°çš„â€œé¦–æ¬¡é€‚é…â€çš„ç´¢å¼•ï¼Œå¹¶ä¸”å¾€ä¸‹åç§»ä¸¤ä¸ªç´¢å¼•
                        recyclerView.smoothScrollToPosition((it + 2).coerceAtMost(mData.size - 1))
                    },
                    500
                )
            }
        }.execute()
    }

    private fun parseViewModel() {
        val viewModel = ViewModelProvider(fragmentActivity!!)[InfoViewModel::class.java]
        platformHelper = viewModel.platformHelper ?: run {
            ZHTools.onBackPressed(fragmentActivity!!)
            return
        }
        mInfoItem = viewModel.infoItem ?: run {
            ZHTools.onBackPressed(fragmentActivity!!)
            return
        }
    }

    private fun loadScreenshots() {
        val progressBar = createProgressView(fragmentActivity!!)
        addMoreView(progressBar)

        Task.runTask {
            platformHelper.getScreenshots(mInfoItem.projectId)
        }.ended(TaskExecutors.getAndroidUI()) { screenshotItems ->
            screenshotItems?.let addButton@{ items ->
                if (items.isEmpty()) return@addButton
                fragmentActivity?.let { activity ->
                    //æ·»åŠ ä¸€ä¸ªæŒ‰é’®ï¼Œé€šè¿‡ç‚¹å‡»è¿™ä¸ªæŒ‰é’®æ¥åŠ è½½å±å¹•æˆªå›¾æ•°æ®
                    addMoreView(AnimButton(activity).apply {
                        layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                        setText(R.string.download_info_load_screenshot)
                        setOnClickListener {
                            setScreenshotView(items)
                            AnimPlayer.play().apply(AnimPlayer.Entry(this, Animations.FadeOut))
                                .setOnEnd { removeMoreView(this) }
                                .start()
                        }
                    })
                }
            }
            removeMoreView(progressBar)
        }.onThrowable { e ->
            Logging.e(
                "DownloadModFragment",
                "Unable to load screenshots, ${Tools.printToString(e)}"
            )
        }.execute()
    }

    @SuppressLint("CheckResult")
    private fun setScreenshotView(screenshotItems: List<ScreenshotItem>) {
        fragmentActivity?.let { activity ->
            val recyclerView = RecyclerView(activity).apply {
                layoutParams = RecyclerView.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                layoutManager = LinearLayoutManager(activity)
                adapter = ScreenshotAdapter(screenshotItems)
            }

            addMoreView(recyclerView)
        }
    }

    private fun createProgressView(context: Context): ProgressBar {
        return ProgressBar(context).apply {
            layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
                gravity = Gravity.CENTER_HORIZONTAL
            }
        }
    }
}

