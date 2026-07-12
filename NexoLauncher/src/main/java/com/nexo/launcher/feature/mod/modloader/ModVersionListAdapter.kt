package com.nexo.launcher.feature.mod.modloader

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.nexo.launcher.anim.animations.Animations
import com.nexo.launcher.R
import com.nexo.launcher.databinding.ItemFileListViewBinding
import com.nexo.launcher.feature.download.item.VersionItem
import com.nexo.launcher.utils.anim.ViewAnimUtils
import com.nexo.launcher.modloaders.FabricVersion
import com.nexo.launcher.modloaders.OptiFineUtils.OptiFineVersion

class ModVersionListAdapter(
    private val iconDrawable: Int,
    private val mData: List<Any>
) :
    RecyclerView.Adapter<ModVersionListAdapter.ViewHolder>() {
    private var onItemClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemFileListViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setView(mData[position])
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.onItemClickListener = listener
    }

    fun interface OnItemClickListener {
        /**
         * @return å¦‚æžœä»»åŠ¡æ­£åœ¨æ‰§è¡Œä¸­ï¼Œéœ€è¦é˜»æ­¢è¿™æ¬¡çš„ç‚¹å‡»äº‹ä»¶ï¼Œåˆ™è¿”å›ž false
         */
        fun onClick(version: Any): Boolean
    }

    inner class ViewHolder(private val binding: ItemFileListViewBinding) : RecyclerView.ViewHolder(binding.root) {
        private val context: Context = itemView.context

        init {
            binding.image.setImageResource(iconDrawable)
            binding.check.visibility = View.GONE
        }

        fun setView(version: Any) {
            when (version) {
                is OptiFineVersion -> binding.name.text = version.versionName
                is FabricVersion -> binding.name.text = version.version
                is VersionItem -> binding.name.text = version.title
                is String -> binding.name.text = version
            }
            itemView.setOnClickListener {
                if (onItemClickListener?.onClick(version) == false) {
                    ViewAnimUtils.setViewAnim(itemView, Animations.Shake)
                    Toast.makeText(context, context.getString(R.string.tasks_ongoing), Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }
            }
        }
    }
}

