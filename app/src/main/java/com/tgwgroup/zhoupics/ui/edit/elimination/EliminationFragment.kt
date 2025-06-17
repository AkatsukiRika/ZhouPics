package com.tgwgroup.zhoupics.ui.edit.elimination

import androidx.fragment.app.activityViewModels
import com.tgwgroup.zhoupics.base.BaseFragment
import com.tgwgroup.zhoupics.databinding.FragmentEliminationBinding
import com.tgwgroup.zhoupics.ui.edit.EditViewModel

class EliminationFragment : BaseFragment<FragmentEliminationBinding>() {
    private val editViewModel by activityViewModels<EditViewModel>()

    companion object {
        const val TAG = "EliminationFragment"
    }

    override fun onBindingCreate(): FragmentEliminationBinding {
        return FragmentEliminationBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        editViewModel.inRoom.value = true
        initTriggers()
    }

    private fun initTriggers() {
        val binding = binding ?: return
        binding.ivCancel.setOnClickListener {
            finishFragment()
        }
    }

    private fun finishFragment() {
        editViewModel.inRoom.value = false
        activity?.supportFragmentManager?.beginTransaction()
            ?.remove(this)
            ?.commitNowAllowingStateLoss()
    }
}