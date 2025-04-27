package com.tgwgroup.zhoupics.ui.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.tgwgroup.zhoupics.databinding.ItemSettingsBinding

/**
 * 设置界面的RecyclerView适配器
 */
class SettingsAdapter : RecyclerView.Adapter<SettingsAdapter.ViewHolder>() {

    private val items: MutableList<SettingsItem> = mutableListOf()

    /**
     * 设置数据列表
     */
    fun setItems(newItems: List<SettingsItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSettingsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemSettingsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SettingsItem) {
            binding.ivIcon.setImageResource(item.icon)
            binding.tvTitle.text = item.title
            binding.ivNext.visibility = if (item.showNext) View.VISIBLE else View.GONE
            
            binding.root.setOnClickListener {
                item.onClick()
            }
        }
    }
} 