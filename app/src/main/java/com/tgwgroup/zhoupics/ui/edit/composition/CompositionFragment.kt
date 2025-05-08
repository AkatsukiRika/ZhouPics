package com.tgwgroup.zhoupics.ui.edit.composition

import androidx.core.view.isInvisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.tgwgroup.zhoupics.base.BaseFragment
import com.tgwgroup.zhoupics.databinding.FragmentCompositionBinding
import com.tgwgroup.zhoupics.ui.edit.EditActivity
import com.tgwgroup.zhoupics.ui.edit.EditViewModel
import com.tgwgroup.zhoupics.utils.collectIn

class CompositionFragment : BaseFragment<FragmentCompositionBinding>() {
    private val viewModel by activityViewModels<CompositionViewModel>()

    private val editViewModel by activityViewModels<EditViewModel>()

    private val cropAdapter = CropAdapter()

    companion object {
        const val TAG = "CompositionFragment"
    }

    override fun onBindingCreate(): FragmentCompositionBinding {
        return FragmentCompositionBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        editViewModel.inRoom.value = true
        initRecyclerView()
        initTriggers()
        initCollectors()

        val binding = binding ?: return
        getEditActivity()?.originalBitmap?.let {
            binding.ivComposition.setImageBitmap(it)
        }
    }

    private fun initRecyclerView() {
        val binding = binding ?: return
        val context = context ?: return
        binding.rvCrop.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvCrop.adapter = cropAdapter
    }

    private fun initTriggers() {
        val binding = binding ?: return
        binding.ivCancel.setOnClickListener {
            finishFragment()
        }
        binding.ivConfirm.setOnClickListener {
            finishFragment()
        }
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        binding.rvCrop.isInvisible = false
                        binding.llRotate.isInvisible = true
                    }

                    1 -> {
                        binding.rvCrop.isInvisible = true
                        binding.llRotate.isInvisible = false
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun initCollectors() {
        viewModel.itemList.collectIn(lifecycleScope) {
            cropAdapter.setItems(it)
        }
    }

    private fun finishFragment() {
        editViewModel.inRoom.value = false
        activity?.supportFragmentManager?.beginTransaction()
            ?.remove(this)
            ?.commitNowAllowingStateLoss()
    }

    private fun getEditActivity(): EditActivity? {
        return activity as? EditActivity
    }
}