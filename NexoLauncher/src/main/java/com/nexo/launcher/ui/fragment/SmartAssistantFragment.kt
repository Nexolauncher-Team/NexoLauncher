package com.nexo.launcher.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.nexo.launcher.anim.AnimPlayer
import com.nexo.launcher.anim.animations.Animations
import com.nexo.launcher.databinding.FragmentSmartAssistantBinding
import com.nexo.launcher.ui.adapter.ChatAdapter
import com.nexo.launcher.ui.viewmodel.SmartAssistantViewModel

class SmartAssistantFragment : FragmentWithAnim() {
    companion object {
        const val TAG = "SmartAssistantFragment"
    }

    private var _binding: FragmentSmartAssistantBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SmartAssistantViewModel
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSmartAssistantBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(SmartAssistantViewModel::class.java)

        chatAdapter = ChatAdapter()
        binding.chatRecycler.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            adapter = chatAdapter
        }

        viewModel.messages.observe(viewLifecycleOwner) {
            chatAdapter.submitList(it.toList())
            if (it.isNotEmpty()) {
                binding.chatRecycler.smoothScrollToPosition(it.size - 1)
            }
        }

        binding.sendButton.setOnClickListener {
            val query = binding.queryInput.text.toString()
            if (query.isNotBlank()) {
                viewModel.sendMessage(query)
                binding.queryInput.text.clear()
            }
        }

        binding.returnButton.setOnClickListener {
            forceBack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun slideIn(animPlayer: AnimPlayer) {
        animPlayer.apply(AnimPlayer.Entry(binding.chatLayout, Animations.BounceInDown))
                  .apply(AnimPlayer.Entry(binding.operateLayout, Animations.BounceInLeft))
    }

    override fun slideOut(animPlayer: AnimPlayer) {
        animPlayer.apply(AnimPlayer.Entry(binding.chatLayout, Animations.FadeOutUp))
                  .apply(AnimPlayer.Entry(binding.operateLayout, Animations.FadeOutRight))
    }
}

