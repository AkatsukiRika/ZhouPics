package com.tgwgroup.zhoupics.ui.promotion

import com.tgwgroup.zhoupics.base.BaseFragment
import com.tgwgroup.zhoupics.databinding.FragmentPromotionBinding

class PromotionFragment : BaseFragment<FragmentPromotionBinding>() {
    companion object {
        const val TAG = "PromotionFragment"
    }

    override fun onBindingCreate(): FragmentPromotionBinding {
        return FragmentPromotionBinding.inflate(layoutInflater)
    }
}