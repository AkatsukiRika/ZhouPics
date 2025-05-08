package com.tgwgroup.zhoupics.ui.edit.composition

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tgwgroup.zhoupics.databinding.ItemCropBinding

class CropAdapter : RecyclerView.Adapter<CropAdapter.ViewHolder>() {
    private val items: MutableList<CropItem> = mutableListOf()

    fun setItems(newItems: List<CropItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCropBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemCropBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CropItem) {
            binding.button.setIconResource(item.icon)
            binding.button.text = item.name

            binding.root.setOnClickListener {
                item.onClick()
            }
        }
    }
} 