package com.nexo.launcher.ui.fragment.settings

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.nexo.launcher.anim.AnimPlayer
import com.nexo.launcher.anim.animations.Animations
import com.nexo.launcher.R
import com.nexo.launcher.databinding.SettingsFragmentSyncBinding
import com.nexo.launcher.firebase.FirebaseBackupManager
import com.nexo.launcher.ui.fragment.FragmentWithAnim

class SyncSettingsFragment() : AbstractSettingsFragment(R.layout.settings_fragment_sync, SettingCategory.SYNC) {
    private lateinit var binding: SettingsFragmentSyncBinding
    private var parentFragment: FragmentWithAnim? = null

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            FirebaseBackupManager.getInstance().handleSignInResult(result.data) { success, error ->
                if (success) {
                    updateSyncUI()
                    Toast.makeText(context, R.string.setting_backup_status_restored, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "${getString(R.string.setting_backup_status_failed)}: $error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    constructor(parentFragment: FragmentWithAnim?) : this() {
        this.parentFragment = parentFragment
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SettingsFragmentSyncBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.googleSignInLayout.setOnClickListener {
            signInLauncher.launch(FirebaseBackupManager.getInstance().getSignInIntent())
        }

        binding.googleSignOutLayout.setOnClickListener {
            FirebaseBackupManager.getInstance().signOut()
            updateSyncUI()
        }

        updateSyncUI()
    }

    private fun updateSyncUI() {
        val manager = FirebaseBackupManager.getInstance()
        if (manager.isLoggedIn()) {
            binding.googleSignInSummary.visibility = View.VISIBLE
            binding.googleSignInSummary.text = getString(R.string.setting_signed_in_as, manager.getUserEmail())
            binding.googleSignOutLayout.visibility = View.VISIBLE
            binding.googleSignInLayout.isEnabled = false
        } else {
            binding.googleSignInSummary.visibility = View.GONE
            binding.googleSignOutLayout.visibility = View.GONE
            binding.googleSignInLayout.isEnabled = true
        }
    }

    override fun slideIn(animPlayer: AnimPlayer) {
        animPlayer.apply(AnimPlayer.Entry(binding.root, Animations.BounceInDown))
    }
}
