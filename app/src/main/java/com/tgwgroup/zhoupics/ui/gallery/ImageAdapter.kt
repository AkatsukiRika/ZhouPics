package com.tgwgroup.zhoupics.ui.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.tgwgroup.zhoupics.databinding.ItemImageBinding
import com.tgwgroup.zhoupics.utils.getBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImageAdapter : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {
    private val items: MutableList<ImageItem> = mutableListOf()
    private val scope = MainScope()
    var canZoom = true

    fun setItems(newItems: List<ImageItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageAdapter.ViewHolder {
        val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ImageAdapter.ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    inner class ViewHolder(private val binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ImageItem) {
            scope.launch(Dispatchers.IO) {
                val bitmap = getBitmap(item.uri, overrideSize = 256)
                withContext(Dispatchers.Main) {
                    binding.ivImage.setImageBitmap(bitmap)
                }
            }
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