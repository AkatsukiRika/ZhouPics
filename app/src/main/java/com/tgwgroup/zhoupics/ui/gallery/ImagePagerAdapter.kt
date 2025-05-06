package com.tgwgroup.zhoupics.ui.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tgwgroup.zhoupics.databinding.ItemImagePageBinding

class ImagePagerAdapter : RecyclerView.Adapter<ImagePagerAdapter.ViewHolder>() {
    private val items: MutableList<AlbumItem> = mutableListOf()

    fun setItems(newItems: List<AlbumItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemImagePageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    inner class ViewHolder(private val binding: ItemImagePageBinding) : RecyclerView.ViewHolder(binding.root) {
        private val imageAdapter = ImageAdapter()

        fun bind(item: AlbumItem) {
            binding.rvImages.apply {
                adapter = imageAdapter
                layoutManager = GridLayoutManager(context, 3)
                imageAdapter.setItems(item.images)
            }
        }
    }
}