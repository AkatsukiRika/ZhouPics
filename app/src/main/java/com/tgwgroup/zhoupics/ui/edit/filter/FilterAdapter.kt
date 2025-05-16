package com.tgwgroup.zhoupics.ui.edit.filter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.tgwgroup.zhoupics.databinding.ItemFilterBinding
import com.tgwgroup.zhoupics.databinding.ItemFilterOriginalBinding
import com.tgwgroup.zhoupics.utils.withAlpha

class FilterAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val VIEW_TYPE_ORIGINAL = 0
        const val VIEW_TYPE_DEFAULT = 1
    }

    private val items: MutableList<FilterItem> = mutableListOf()

    fun setItems(newItems: List<FilterItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_ORIGINAL) {
            val binding = ItemFilterOriginalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return OriginalViewHolder(binding)
        } else {
            val binding = ItemFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = items[position]
        return if (item.id == FILTER_ORIGINAL) {
            VIEW_TYPE_ORIGINAL
        } else {
            VIEW_TYPE_DEFAULT
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is ViewHolder) {
            holder.bind(item)
        } else if (holder is OriginalViewHolder) {
            holder.bind(item)
        }
    }

    inner class ViewHolder(private val binding: ItemFilterBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FilterItem) {
            binding.ivFilter.setImageResource(item.icon)
            binding.tvName.text = item.name
            binding.tvName.setBackgroundColor(item.labelBgColor)
            binding.flSelected.setBackgroundColor(item.labelBgColor.withAlpha(75))
            binding.ivFilter.setOnClickListener {
                item.onClick()
            }
            binding.flSelected.isVisible = item.selected
        }
    }

    inner class OriginalViewHolder(private val binding: ItemFilterOriginalBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FilterItem) {
            binding.ivFilter.setOnClickListener {
                item.onClick()
            }
            binding.flSelected.isVisible = item.selected
        }
    }
}