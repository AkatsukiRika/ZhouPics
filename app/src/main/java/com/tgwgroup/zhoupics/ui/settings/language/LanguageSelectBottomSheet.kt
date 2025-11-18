package com.tgwgroup.zhoupics.ui.settings.language

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.tgwgroup.zhoupics.base.BaseBottomSheet
import com.tgwgroup.zhoupics.databinding.LayoutLanguageSelectBinding

class LanguageSelectBottomSheet : BaseBottomSheet<LayoutLanguageSelectBinding>() {
    companion object {
        const val TAG = "LanguageSelectBottomSheet"

        fun show(fragmentManager: FragmentManager, action: ((LanguageSelectBottomSheet) -> Unit)? = null) {
            val dialog = LanguageSelectBottomSheet().apply {
                action?.invoke(this)
            }
            dialog.show(fragmentManager, TAG)
        }
    }

    private val languageAdapter = LanguageAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            dismiss()
        }
    }

    override fun initView() {
        super.initView()
        val context = context ?: return
        val binding = binding ?: return
        binding.rvLanguage.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = languageAdapter
            languageAdapter.setItems(getSupportLanguageList())
        }
    }

    override fun onBindingCreate(inflater: LayoutInflater, container: ViewGroup?): LayoutLanguageSelectBinding {
        return LayoutLanguageSelectBinding.inflate(inflater, container, false)
    }
}