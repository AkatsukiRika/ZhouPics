package com.tgwgroup.zhoupics.ui.settings.language

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.tgwgroup.zhoupics.base.BaseBottomSheet
import com.tgwgroup.zhoupics.databinding.LayoutLanguageSelectBinding
import com.tgwgroup.zhoupics.language.LanguageHelper
import com.tgwgroup.zhoupics.language.changeLanguage

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

    private val languageList = getSupportLanguageList()

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

        LanguageHelper.getSavedLanguage(context)?.let { savedLanguage ->
            languageList.forEach {
                it.selected = it.name == savedLanguage
            }
        }

        binding.rvLanguage.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = languageAdapter
        }
        languageAdapter.apply {
            setItems(languageList)
            onSelect = { item ->
                languageList.forEach {
                    it.selected = it.name == item.name
                }
                notifyItemRangeChanged(0, languageList.size, 0)
            }
        }
        binding.tvDownloadNow.setOnClickListener {
            val selectedLanguage = languageList.firstOrNull { it.selected }
            selectedLanguage?.let {
                changeLanguage(it.name)
            }
            dismissAllowingStateLoss()
        }
    }

    override fun onBindingCreate(inflater: LayoutInflater, container: ViewGroup?): LayoutLanguageSelectBinding {
        return LayoutLanguageSelectBinding.inflate(inflater, container, false)
    }
}