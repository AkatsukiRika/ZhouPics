package com.tgwgroup.zhoupics.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class BaseBottomSheet<T : ViewBinding> : BottomSheetDialogFragment() {
    protected var binding: T? = null

    abstract fun onBindingCreate(inflater: LayoutInflater, container: ViewGroup?): T

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = onBindingCreate(inflater, container)
        initView()
        return binding!!.root
    }

    final override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    open fun initView() {}
}