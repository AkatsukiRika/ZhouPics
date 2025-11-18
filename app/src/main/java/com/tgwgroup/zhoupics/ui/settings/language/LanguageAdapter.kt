package com.tgwgroup.zhoupics.ui.settings.language

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tgwgroup.zhoupics.R
import com.tgwgroup.zhoupics.databinding.ItemLanguageBinding
import com.tgwgroup.zhoupics.utils.appContext

class LanguageAdapter : RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {
    private val items: MutableList<LanguageItem> = mutableListOf()

    var onSelect: ((LanguageItem) -> Unit)? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(newItems: List<LanguageItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(private val binding: ItemLanguageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LanguageItem) {
            binding.tvName.text = item.displayName
            if (item.selected) {
                binding.root.backgroundTintList = appContext.getColorStateList(R.color.theme_opposite_20p)
            } else {
                binding.root.backgroundTintList = appContext.getColorStateList(R.color.transparent)
            }
            binding.root.setOnClickListener {
                onSelect?.invoke(item)
            }
        }
    }
}