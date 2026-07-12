package com.nexo.launcher.ui.fragment.download.addon

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.nexo.launcher.R
import com.nexo.launcher.event.sticky.SelectInstallTaskEvent
import com.nexo.launcher.feature.log.Logging
import com.nexo.launcher.feature.mod.modloader.ModVersionListAdapter
import com.nexo.launcher.feature.mod.modloader.NeoForgeDownloadTask
import com.nexo.launcher.feature.mod.modloader.NeoForgeUtils.Companion.downloadNeoForgeVersions
import com.nexo.launcher.feature.mod.modloader.NeoForgeUtils.Companion.downloadNeoForgedForgeVersions
import com.nexo.launcher.feature.mod.modloader.NeoForgeUtils.Companion.formatGameVersion
import com.nexo.launcher.feature.version.install.Addon
import com.nexo.launcher.task.TaskExecutors
import com.nexo.launcher.ui.fragment.InstallGameFragment.Companion.BUNDLE_MC_VERSION
import com.nexo.launcher.ui.subassembly.modlist.ModListFragment
import com.nexo.launcher.utils.ZHTools
import com.nexo.launcher.Tools
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.Future
import java.util.function.Consumer

class DownloadNeoForgeFragment : ModListFragment() {
    companion object {
        const val TAG: String = "DownloadNeoForgeFragment"
    }

    override fun refreshCreatedView() {
        setIcon(ContextCompat.getDrawable(fragmentActivity!!, R.drawable.ic_neoforge))
        setTitleText("NeoForge")
        setLink("https://neoforged.net/")
        setMCMod("https://www.mcmod.cn/class/11433.html")
        setReleaseCheckBoxGone() //éšè—â€œä»…å±•ç¤ºæ­£å¼ç‰ˆâ€é€‰æ‹©æ¡†ï¼Œåœ¨è¿™é‡Œæ²¡æœ‰ç”¨å¤„
    }

    override fun initRefresh(): Future<*> {
        return refresh(false)
    }

    override fun refresh(): Future<*> {
        return refresh(true)
    }

    private fun refresh(force: Boolean): Future<*> {
        return TaskExecutors.getDefault().submit {
            runCatching {
                TaskExecutors.runInUIThread {
                    cancelFailedToLoad()
                    componentProcessing(true)
                }
                processModDetails(loadVersionList(force))
            }.getOrElse { e ->
                TaskExecutors.runInUIThread {
                    componentProcessing(false)
                    setFailedToLoad(e.toString())
                }
                Logging.e("DownloadNeoForgeFragment", Tools.printToString(e))
            }
        }
    }

    @Throws(Exception::class)
    fun loadVersionList(force: Boolean): List<String> {
        val versions: MutableList<String> = ArrayList()
        versions.addAll(downloadNeoForgedForgeVersions(force))
        versions.addAll(downloadNeoForgeVersions(force))

        versions.reverse()

        return versions
    }

    private fun empty() {
        TaskExecutors.runInUIThread {
            componentProcessing(false)
            setFailedToLoad(getString(R.string.version_install_no_versions))
        }
    }

    private fun processModDetails(neoForgeVersions: List<String>?) {
        neoForgeVersions ?: run {
            empty()
            return
        }

        val mcVersion = arguments?.getString(BUNDLE_MC_VERSION) ?: throw IllegalArgumentException("The Minecraft version is not passed")

        val mNeoForgeVersions: MutableMap<String, MutableList<String>> = HashMap()
        neoForgeVersions.forEach(Consumer { neoForgeVersion: String ->
            currentTask?.apply { if (isCancelled) return@Consumer }
            //æŸ¥æ‰¾å¹¶åˆ†ç»„Minecraftç‰ˆæœ¬ä¸ŽNeoForgeç‰ˆæœ¬
            val gameVersion = if (neoForgeVersion == "47.1.82") {
                return@Consumer
            } else {
                formatGameVersion(neoForgeVersion)
            }
            addIfAbsent(mNeoForgeVersions, gameVersion, neoForgeVersion)
        })

        currentTask?.apply { if (isCancelled) return }

        val mcNeoForgeVersions = mNeoForgeVersions[mcVersion] ?: run {
            empty()
            return
        }

        val adapter = ModVersionListAdapter(R.drawable.ic_neoforge, mcNeoForgeVersions)
        adapter.setOnItemClickListener { version: Any? ->
            if (isTaskRunning()) return@setOnItemClickListener false

            val versionString = version.toString()
            EventBus.getDefault().postSticky(
                SelectInstallTaskEvent(
                    Addon.NEOFORGE,
                    versionString,
                    NeoForgeDownloadTask(versionString)
                )
            )
            ZHTools.onBackPressed(requireActivity())
            true
        }

        currentTask?.apply { if (isCancelled) return }

        TaskExecutors.runInUIThread {
            val recyclerView = recyclerView
            runCatching {
                recyclerView.layoutManager = LinearLayoutManager(fragmentActivity!!)
                recyclerView.adapter = adapter
            }.getOrElse { e ->
                Logging.e("Set Adapter", Tools.printToString(e))
            }

            componentProcessing(false)
            recyclerView.scheduleLayoutAnimation()
        }
    }
}

