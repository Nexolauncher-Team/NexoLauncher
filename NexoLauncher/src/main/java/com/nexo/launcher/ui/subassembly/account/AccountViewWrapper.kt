package com.nexo.launcher.ui.subassembly.account

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import com.nexo.launcher.R
import com.nexo.launcher.databinding.ViewAccountBinding
import com.nexo.launcher.feature.accounts.AccountUtils
import com.nexo.launcher.feature.accounts.AccountsManager
import com.nexo.launcher.feature.log.Logging
import com.nexo.launcher.ui.fragment.AccountFragment
import com.nexo.launcher.ui.fragment.FragmentWithAnim
import com.nexo.launcher.utils.ZHTools
import com.nexo.launcher.utils.skin.SkinLoader
import com.nexo.launcher.Tools

class AccountViewWrapper(private val parentFragment: FragmentWithAnim? = null, val binding: ViewAccountBinding) {
    private val mContext: Context = binding.root.context

    init {
        parentFragment?.let { fragment ->
            binding.root.setOnClickListener {
                ZHTools.swapFragmentWithAnim(fragment, AccountFragment::class.java, AccountFragment.TAG, null)
            }
        }
    }

    fun refreshAccountInfo() {
        binding.apply {
            val account = AccountsManager.currentAccount
            account ?: run {
                if (parentFragment == null) {
                    userIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_help))
                    userName.text = null
                } else {
                    userIcon.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_add))
                    userName.setText(R.string.account_add)
                }
                accountType.visibility = View.GONE
                return
            }

            runCatching {
                userIcon.setImageDrawable(
                    SkinLoader.getAvatarDrawable(
                        mContext,
                        account,
                        Tools.dpToPx(
                            mContext.resources.getDimensionPixelSize(R.dimen._52sdp).toFloat()
                        ).toInt()
                    )
                )
            }.onFailure { e ->
                Logging.e("AccountViewWrapper", "Failed to load avatar.", e)
            }

            userName.text = account.username
            accountType.text = AccountUtils.getAccountTypeName(mContext, account)
            accountType.visibility = View.VISIBLE
        }
    }
}

