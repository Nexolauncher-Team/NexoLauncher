package com.nexo.launcher.ui.fragment.about

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.nexo.launcher.InfoCenter
import com.nexo.launcher.InfoDistributor
import com.nexo.launcher.R
import com.nexo.launcher.databinding.FragmentAboutInfoPageBinding
import com.nexo.launcher.ui.dialog.TipDialog
import com.nexo.launcher.ui.subassembly.about.AboutItemBean
import com.nexo.launcher.ui.subassembly.about.AboutItemBean.AboutItemButtonBean
import com.nexo.launcher.ui.subassembly.about.AboutRecyclerAdapter
import com.nexo.launcher.utils.ZHTools
import com.nexo.launcher.utils.path.UrlManager

class AboutInfoPageFragment() : Fragment(R.layout.fragment_about_info_page) {
    private lateinit var binding: FragmentAboutInfoPageBinding
    private val mAboutData: MutableList<AboutItemBean> = ArrayList()
    private var parentPager2: ViewPager2? = null

    constructor(parentPager: ViewPager2): this() {
        this.parentPager2 = parentPager
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAboutInfoPageBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadAboutData(requireContext().resources)

        val context = requireActivity()

        binding.apply {
            dec1.text = InfoCenter.replaceName(context, R.string.about_dec1)
            dec2.text = InfoCenter.replaceName(context, R.string.about_dec2)
            dec3.text = InfoCenter.replaceName(context, R.string.about_dec3)

            githubButton.setOnClickListener { ZHTools.openLink(requireActivity(), UrlManager.URL_HOME) }
            licenseButton.setOnClickListener { ZHTools.openLink(requireActivity(), "https://www.gnu.org/licenses/gpl-3.0.html") }

            val aboutAdapter = AboutRecyclerAdapter(this@AboutInfoPageFragment.mAboutData)
            aboutRecycler.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = aboutAdapter
            }
            sponsor.setOnClickListener { _ ->
                parentPager2?.currentItem = 1
            }

            if (ZHTools.isChinese(requireActivity())) {
                qqGroupButton.visibility = View.VISIBLE
                qqGroupButton.setOnClickListener {
                    TipDialog.Builder(context)
                        .setTitle("QQ")
                        .setMessage("æ¬¢è¿ŽåŠ å…¥ ${InfoDistributor.APP_NAME} å®˜æ–¹ QQ äº¤æµç¾¤ï¼ˆç¾¤å·ï¼š${InfoCenter.QQ_GROUP}ï¼‰ï¼ç”±äºŽç¾¤äººæ•°æœ‰é™ï¼ŒåŠ å…¥ç¾¤èŠå‰éœ€è¦èµžåŠ© 5å…ƒ æˆ–ä»¥ä¸Šé‡‘é¢ï¼Œè¯·ç‚¹å‡»å³ä¾§â€œèµžåŠ©å¼€å‘â€æŒ‰é’®è®¿é—®çˆ±å‘ç”µã€‚")
                        .setSelectable(true)
                        .setConfirm(R.string.generic_confirm)
                        .setShowCancel(false)
                        .showDialog()
                }
            } else {
                qqGroupButton.visibility = View.GONE
            }

            discordButton.setOnClickListener { ZHTools.openLink(requireActivity(), "https://discord.gg/yDDkTHp4cJ") }

            privacyPolicyButton.setOnClickListener { ZHTools.openLink(requireActivity(), UrlManager.URL_PRIVACY_POLICY) }
            termsOfServiceButton.setOnClickListener { ZHTools.openLink(requireActivity(), UrlManager.URL_TERMS_OF_SERVICE) }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun loadAboutData(resources: Resources) {
        mAboutData.clear()

        mAboutData.add(
            AboutItemBean(
                resources.getDrawable(R.drawable.ic_pojav_full, requireContext().theme),
                "PojavLauncherTeam",
                getString(R.string.about_PojavLauncher_desc),
                AboutItemButtonBean(requireActivity(), "Github", "https://github.com/PojavLauncherTeam/PojavLauncher")
            )
        )
        mAboutData.add(
            AboutItemBean(
                resources.getDrawable(R.drawable.image_about_movtery, requireContext().theme),
                "Sameer Yadav",
                getString(R.string.about_MovTery_desc),
                AboutItemButtonBean(
                    requireActivity(),
                    "Soon",
                    "#"
                )
            )
        )
        mAboutData.add(
            AboutItemBean(
                resources.getDrawable(R.drawable.image_about_mcmod, requireContext().theme),
                "MC ç™¾ç§‘",
                getString(R.string.about_mcmod_desc),
                AboutItemButtonBean(
                    requireActivity(),
                    getString(R.string.about_access_link),
                    UrlManager.URL_MCMOD)
            )
        )
        mAboutData.add(
            AboutItemBean(
                resources.getDrawable(R.drawable.image_about_verafirefly, requireContext().theme),
                "Vera-Firefly",
                getString(R.string.about_VeraFirefly_desc),
                AboutItemButtonBean(
                    requireActivity(),
                    getString(R.string.about_access_space),
                    "https://space.bilibili.com/1412062866"
                )
            )
        )
        mAboutData.add(
            AboutItemBean(
                resources.getDrawable(R.drawable.image_about_lingmuqiuzhu, requireContext().theme),
                "æŸƒæœ¨æ¹«ç«¹",
                getString(R.string.about_LingMuQiuZhu_desc),
                AboutItemButtonBean(
                    requireActivity(),
                    getString(R.string.about_access_space),
                    "https://space.bilibili.com/515165764"
                )
            )
        )
        mAboutData.add(
            AboutItemBean(
                resources.getDrawable(R.drawable.image_about_shirosakimio, requireContext().theme),
                "ShirosakiMio",
                getString(R.string.about_ShirosakiMio_desc),
                AboutItemButtonBean(
                    requireActivity(),
                    getString(R.string.about_access_space),
                    "https://space.bilibili.com/35801833"
                )
            )
        )
        mAboutData.add(
            AboutItemBean(
                resources.getDrawable(R.drawable.image_about_bangbang93, requireContext().theme),
                "bangbang93",
                getString(R.string.about_bangbang93_desc),
                AboutItemButtonBean(
                    requireActivity(),
                    getString(R.string.about_button_support_development),
                    "https://afdian.com/a/bangbang93"
                )
            )
        )
    }
}


