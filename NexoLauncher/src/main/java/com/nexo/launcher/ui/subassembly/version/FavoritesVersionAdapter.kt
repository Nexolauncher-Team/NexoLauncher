package com.nexo.launcher.ui.subassembly.version

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nexo.launcher.databinding.ItemFileListViewBinding
import com.nexo.launcher.feature.version.favorites.FavoritesVersionUtils

class FavoritesVersionAdapter(private val versionName: String) : RecyclerView.Adapter<FavoritesVersionAdapter.ViewHolder>() {
    private val allCategories = FavoritesVersionUtils.getFavoritesStructure().keys.toList()
    private val favoritesMap = FavoritesVersionUtils.getFavoritesStructure()
    private val selectedCategorySet: MutableSet<String> = HashSet()

    init {
        //æ‰¾åˆ°å½“å‰æ”¶è—äº†å½“å‰ç‰ˆæœ¬çš„æ”¶è—å¤¹ï¼Œæ·»åŠ è¿›selectedCategoryList
        favoritesMap.forEach { (categoryName, versions) ->
            if (versions.contains(versionName)) {
                selectedCategorySet.add(categoryName)
            }
        }
    }

    /**
     * @return èŽ·å–å½“å‰å·²ç»é€‰æ‹©çš„æ”¶è—å¤¹åç§°
     */
    fun getSelectedCategorySet() = selectedCategorySet

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemFileListViewBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(allCategories[position])
    }

    override fun getItemCount(): Int = allCategories.size

    inner class ViewHolder(private val binding: ItemFileListViewBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(categoryName: String) {
            binding.apply {
                image.visibility = View.GONE
                name.text = categoryName

                check.setOnClickListener(null)

                if (selectedCategorySet.contains(categoryName)) {
                    check.isChecked = true
                }

                root.setOnClickListener { check.isChecked = !check.isChecked }
                check.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) selectedCategorySet.add(categoryName)
                    else selectedCategorySet.remove(categoryName)
                }
            }
        }
    }
}
