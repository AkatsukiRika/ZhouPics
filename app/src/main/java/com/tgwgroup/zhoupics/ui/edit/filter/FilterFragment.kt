package com.tgwgroup.zhoupics.ui.edit.filter

import androidx.core.view.isInvisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tgwgroup.zhoupics.base.BaseFragment
import com.tgwgroup.zhoupics.databinding.FragmentFilterBinding
import com.tgwgroup.zhoupics.ui.edit.EditActivity
import com.tgwgroup.zhoupics.utils.collectIn
import com.tgwgroup.zhoupics.utils.dpToPx

class FilterFragment : BaseFragment<FragmentFilterBinding>() {
    private val filterAdapter = FilterAdapter()

    private val viewModel by viewModels<FilterViewModel>()

    companion object {
        const val TAG = "FilterFragment"
    }

    override fun onBindingCreate(): FragmentFilterBinding {
        return FragmentFilterBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        viewModel.init()
        initRecyclerView()
        initCollectors()

        val binding = binding ?: return
        binding.slider.bindBubble(binding.sliderBubble)
        binding.rvFilter.post {
            getEditActivity()?.tabFragmentBodyHeight?.value = binding.rvFilter.height + dpToPx(8f)
            getEditActivity()?.tabFragmentSliderHeight?.value = 0
        }
    }

    private fun initRecyclerView() {
        val binding = binding ?: return
        val context = context ?: return
        binding.rvFilter.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvFilter.adapter = filterAdapter
    }

    private fun initCollectors() {
        viewModel.itemList.collectIn(lifecycleScope) {
            filterAdapter.setItems(it)
        }

        viewModel.selectedItemId.collectIn(lifecycleScope) {
            onSelectedItemChanged(it)
        }
    }

    private fun onSelectedItemChanged(itemId: Int?) {
        val binding = binding ?: return
        val item = viewModel.itemList.value.find { it.id == itemId }
        item?.let {
            binding.slider.post {
                if (binding.slider.isInvisible) {
                    binding.vSliderGradient.isInvisible = false
                    binding.slider.isInvisible = false
                    getEditActivity()?.tabFragmentSliderHeight?.value = binding.slider.height
                }
            }
        }
    }

    private fun getEditActivity(): EditActivity? {
        return activity as? EditActivity
    }
}