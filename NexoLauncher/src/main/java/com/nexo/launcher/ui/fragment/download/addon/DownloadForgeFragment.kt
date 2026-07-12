package com.nexo.launcher.ui.fragment.download.addon

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.nexo.launcher.R
import com.nexo.launcher.event.sticky.SelectInstallTaskEvent
import com.nexo.launcher.feature.log.Logging
import com.nexo.launcher.feature.mod.modloader.ModVersionListAdapter
import com.nexo.launcher.task.TaskExecutors
import com.nexo.launcher.ui.subassembly.modlist.ModListFragment
import com.nexo.launcher.utils.ZHTools
import com.nexo.launcher.Tools
import com.nexo.launcher.feature.mod.modloader.ForgeDownloadTask
import com.nexo.launcher.feature.version.install.Addon
import com.nexo.launcher.ui.fragment.InstallGameFragment.Companion.BUNDLE_MC_VERSION
import com.nexo.launcher.modloaders.ForgeUtils
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.Future
import java.util.function.Consumer

class DownloadForgeFragment : ModListFragment() {
    companion object {
        const val TAG: String = "DownloadForgeFragment"
    }

    override fun refreshCreatedView() {
        setIcon(ContextCompat.getDrawable(fragmentActivity!!, R.drawable.ic_anvil))
        setTitleText("Forge")
        setLink("https://forums.minecraftforge.net/")
        setMCMod("https://www.mcmod.cn/class/30.html")
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
                val forgeVersions = ForgeUtils.downloadForgeVersions(force)
                processModDetails(forgeVersions)
            }.getOrElse { e ->
                TaskExecutors.runInUIThread {
                    componentProcessing(false)
                    setFailedToLoad(e.toString())
                }
                Logging.e("DownloadForge", Tools.printToString(e))
            }
        }
    }

    private fun empty() {
        TaskExecutors.runInUIThread {
            componentProcessing(false)
            setFailedToLoad(getString(R.string.version_install_no_versions))
        }
    }

    private fun processModDetails(forgeVersions: List<String>?) {
        forgeVersions ?: run {
            empty()
            return
        }

        val mcVersion = arguments?.getString(BUNDLE_MC_VERSION) ?: throw IllegalArgumentException("The Minecraft version is not passed")

        val mForgeVersions: MutableMap<String, MutableList<String>> = HashMap()
        forgeVersions.forEach(Consumer { forgeVersion: String ->
            currentTask?.apply { if (isCancelled) return@Consumer }

            //æŸ¥æ‰¾å¹¶åˆ†ç»„Minecraftç‰ˆæœ¬ä¸ŽForgeç‰ˆæœ¬
            val dashIndex = forgeVersion.indexOf("-")
            val gameVersion = forgeVersion.substring(0, dashIndex)
            addIfAbsent(mForgeVersions, gameVersion, forgeVersion)
        })

        currentTask?.apply { if (isCancelled) return }

        val mcForgeVersions = mForgeVersions[mcVersion] ?: run {
            empty()
            return
        }

        val adapter = ModVersionListAdapter(R.drawable.ic_anvil, mcForgeVersions)
        adapter.setOnItemClickListener { version: Any ->
            if (isTaskRunning()) return@setOnItemClickListener false

            val versionString = version.toString()
            EventBus.getDefault().postSticky(
                SelectInstallTaskEvent(
                    Addon.FORGE,
                    versionString,
                    ForgeDownloadTask(versionString)
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

