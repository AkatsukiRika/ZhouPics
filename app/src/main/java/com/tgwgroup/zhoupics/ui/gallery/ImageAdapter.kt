package com.tgwgroup.zhoupics.ui.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.tgwgroup.zhoupics.databinding.ItemImageBinding
import com.tgwgroup.zhoupics.utils.loadImage

class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {
    private val items: MutableList<ImageItem> = mutableListOf()
    var canZoom = true

    fun setItems(newItems: List<ImageItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    inner class ViewHolder(private val binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ImageItem) {
            binding.ivImage.loadImage(item.uri)
            binding.ivZoom.isVisible = canZoom
            binding.ivImage.setOnClickListener {
                item.onClick?.invoke(ImageClickEvent.GO_EDIT)
            }
            binding.ivZoom.setOnClickListener {
                item.onClick?.invoke(ImageClickEvent.ZOOM)
            }
        }
    }
}