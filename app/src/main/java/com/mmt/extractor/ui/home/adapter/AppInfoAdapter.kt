package com.mmt.extractor.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mmt.extractor.databinding.ItemAppBinding
import com.mmt.extractor.domain.model.AppInfo
import java.util.Locale

class AppInfoAdapter(val itemClickCallback: (Pair<AppInfo, ImageView>) -> Unit) : ListAdapter<AppInfo, AppInfoAdapter.ViewHolder>(AppInfoDiffCallback()) {
    class AppInfoDiffCallback : DiffUtil.ItemCallback<AppInfo>() {
        override fun areItemsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AppInfo, newItem: AppInfo): Boolean {
            return oldItem.pkgName == newItem.pkgName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemAppBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemAppBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                getItem(adapterPosition)?.let {
                    itemClickCallback(it to binding.ivAppPreview)
                }
            }
        }

        fun bind(appInfo: AppInfo) {
            with(binding) {
                tvAppNames.text = appInfo.appName
                tvPackageName.text = appInfo.pkgName
                tvAppSize.text = String.format(Locale.getDefault(), "%.2f MB", appInfo.apkSize)
                ivAppPreview.setImageDrawable(appInfo.appIcon)
            }
        }
    }
}
