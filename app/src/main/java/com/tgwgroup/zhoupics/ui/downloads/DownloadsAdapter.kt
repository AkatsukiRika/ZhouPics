package com.tgwgroup.zhoupics.ui.downloads

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tgwgroup.zhoupics.databinding.ItemDownloadsBinding

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
        fun bind(item: DownloadsItem) {
            binding.tvTitle.text = item.title
            binding.tvFileName.text = item.fileName
            binding.tvFileSize.text = item.getHumanizedSize()
        }
    }
}