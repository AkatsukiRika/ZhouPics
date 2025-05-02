package com.tgwgroup.zhoupics.ui.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.tgwgroup.zhoupics.R
import com.tgwgroup.zhoupics.databinding.ItemAlbumBinding
import com.tgwgroup.zhoupics.utils.dpToPx

class AlbumAdapter : RecyclerView.Adapter<AlbumAdapter.ViewHolder>() {
    private val items: MutableList<AlbumItem> = mutableListOf()

    fun setItems(newItems: List<AlbumItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAlbumBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemAlbumBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AlbumItem) {
            binding.tvName.text = item.name
            binding.root.setOnClickListener {
                item.onClick()
            }
            if (item.selected) {
                binding.root.setBackgroundResource(R.drawable.bg_album_selected)
            } else {
                binding.root.setBackgroundResource(R.drawable.bg_album_unselected)
            }
            binding.root.updateLayoutParams<MarginLayoutParams> {
                marginEnd = if (adapterPosition == items.lastIndex) dpToPx(8f) else 0
            }
        }
    }
}