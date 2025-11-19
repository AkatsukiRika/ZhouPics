package com.tgwgroup.zhoupics.ui.promotion

import com.tgwgroup.zhoupics.R
import com.tgwgroup.zhoupics.base.BaseFragment
import com.tgwgroup.zhoupics.databinding.FragmentPromotionBinding
import com.tgwgroup.zhoupics.utils.openUrl

class PromotionFragment : BaseFragment<FragmentPromotionBinding>() {
    companion object {
        const val TAG = "PromotionFragment"
    }

    override fun onBindingCreate(): FragmentPromotionBinding {
        return FragmentPromotionBinding.inflate(layoutInflater)
    }

    override fun initView() {
        val context = context ?: return
        val binding = binding ?: return
        binding.clCard1.setOnClickListener {
            context.openUrl(context.resources.getString(R.string.github_link))
        }
        binding.clCard2.setOnClickListener {
            context.openUrl(context.resources.getString(R.string.personal_website_link))
        }
        binding.clCard3.setOnClickListener {
            context.openUrl(context.resources.getString(R.string.zhoutools_link))
        }
    }
}