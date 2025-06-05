package com.tgwgroup.zhoupics.ui.downloads

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.tgwgroup.zhoupics.R
import com.tgwgroup.zhoupics.databinding.ItemDownloadsBinding
import com.tgwgroup.zhoupics.utils.appContext

class DownloadsAdapter : RecyclerView.Adapter<DownloadsAdapter.ViewHolder>() {
    private val items: MutableList<DownloadsItem> = mutableListOf()

    fun setItems(newItems: List<DownloadsItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDownloadsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    inner class ViewHolder(private val binding: ItemDownloadsBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(item: DownloadsItem) {
            binding.tvTitle.text = item.title
            binding.tvFileName.text = item.fileName
            binding.tvFileSize.text = item.getHumanizedSize(item.fileSizeBytes)
            when (item.downloadStatus) {
                DownloadStatus.NOT_STARTED -> {
                    binding.btnDownload.isInvisible = false
                    binding.btnDownload.text = appContext.getString(R.string.download)
                }
                DownloadStatus.IN_PROGRESS -> {
                    binding.btnDownload.isInvisible = true
                    binding.tvFileSize.text = "${item.getHumanizedSize(item.totalBytesRead)} / ${item.getHumanizedSize(item.fileSizeBytes)}"
                    binding.vDownloadProgress.progress = item.totalBytesRead.toFloat() * 100 / item.fileSizeBytes
                }
                DownloadStatus.COMPLETED -> {
                    binding.btnDownload.isInvisible = true
                    binding.tvFileSize.text = item.getHumanizedSize(item.fileSizeBytes)
                    binding.vDownloadProgress.progress = 100f
                }
                DownloadStatus.FAILED -> {
                    binding.btnDownload.isInvisible = false
                    binding.btnDownload.text = appContext.getString(R.string.retry)
                }
            }
            binding.btnDownload.setOnClickListener {
                item.onClick(item)
            }
        }
    }
}