package com.tgwgroup.zhoupics.ui.edit.compare

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.tgwgroup.zhoupics.base.BaseBottomSheet
import com.tgwgroup.zhoupics.databinding.LayoutCompareModeSelectBinding

class CompareModeSelectBottomSheet : BaseBottomSheet<LayoutCompareModeSelectBinding>() {
    companion object {
        const val TAG = "CompareModeSelectBottomSheet"

        fun show(fragmentManager: FragmentManager, action: ((CompareModeSelectBottomSheet) -> Unit)? = null) {
            val dialog = CompareModeSelectBottomSheet().apply {
                action?.invoke(this)
            }
            dialog.show(fragmentManager, TAG)
        }
    }

    var onPreciseModeClick: (() -> Unit)? = null

    var onFastModeClick: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            dismiss()
        }
    }

    override fun onBindingCreate(inflater: LayoutInflater, container: ViewGroup?): LayoutCompareModeSelectBinding {
        return LayoutCompareModeSelectBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        super.initView()
        val binding = binding ?: return
        binding.tvPreciseMode.setOnClickListener {
            onPreciseModeClick?.invoke()
            dismissAllowingStateLoss()
        }
        binding.tvFastMode.setOnClickListener {
            onFastModeClick?.invoke()
            dismissAllowingStateLoss()
        }
    }
}