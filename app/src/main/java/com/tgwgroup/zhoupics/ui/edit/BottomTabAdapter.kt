package com.tgwgroup.zhoupics.ui.edit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.tgwgroup.zhoupics.R
import com.tgwgroup.zhoupics.databinding.ItemBottomTabBinding

class BottomTabAdapter : RecyclerView.Adapter<BottomTabAdapter.ViewHolder>() {
    private val items: MutableList<BottomTabItem> = mutableListOf()

    fun setItems(newItems: List<BottomTabItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBottomTabBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount() = items.size

    inner class ViewHolder(private val binding: ItemBottomTabBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BottomTabItem) {
            val context = binding.root.context
            binding.tvName.text = item.name
            if (item.selected) {
                binding.tvName.setTextColor(context.resources.getColor(R.color.theme, null))
                binding.vIndicator.isInvisible = false
            } else {
                binding.tvName.setTextColor(context.resources.getColor(R.color.white_50p, null))
                binding.vIndicator.isInvisible = true
            }
            binding.root.setOnClickListener {
                item.onClick()
            }
        }
    }
}