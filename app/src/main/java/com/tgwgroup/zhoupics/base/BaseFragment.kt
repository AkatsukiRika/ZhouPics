package com.tgwgroup.zhoupics.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<T : ViewBinding> : Fragment() {
    protected var binding: T? = null

    abstract fun onBindingCreate(): T

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = onBindingCreate()
        initView()
        return binding!!.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        destroyView()
        binding = null
    }

    open fun initView() {}

    open fun destroyView() {}
}