package com.tgwgroup.zhoupics.ui.edit.adjust

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tgwgroup.zhoupics.R
import com.tgwgroup.zhoupics.databinding.ItemAdjustBinding

class AdjustAdapter : RecyclerView.Adapter<AdjustAdapter.ViewHolder>() {

    private val items: MutableList<AdjustItem> = mutableListOf()

    fun setItems(newItems: List<AdjustItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAdjustBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemAdjustBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AdjustItem) {
            val context = binding.root.context

            binding.ivIcon.setImageResource(item.icon)
            binding.tvName.text = item.name
            if (item.selected) {
                binding.ivIcon.imageTintList = context.getColorStateList(R.color.theme)
                binding.tvName.setTextColor(context.getColor(R.color.theme))
            } else {
                binding.ivIcon.imageTintList = context.getColorStateList(R.color.white)
                binding.tvName.setTextColor(context.getColor(R.color.white))
            }

            binding.root.setOnClickListener {
                item.onClick()
            }
        }
    }
} 