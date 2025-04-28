package com.tgwgroup.zhoupics.ui.edit.beautify

import androidx.core.view.isInvisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tgwgroup.zhoupics.base.BaseFragment
import com.tgwgroup.zhoupics.databinding.FragmentBeautifyBinding
import com.tgwgroup.zhoupics.utils.collectIn
import com.tgwgroup.zhoupics.widgets.BidirectionalSlider

class BeautifyFragment : BaseFragment<FragmentBeautifyBinding>() {
    private val beautifyAdapter = BeautifyAdapter()

    private val viewModel by viewModels<BeautifyViewModel>()

    companion object {
        const val TAG = "BeautifyFragment"
    }

    override fun onBindingCreate(): FragmentBeautifyBinding {
        return FragmentBeautifyBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        initRecyclerView()
        initListeners()
        initCollectors()

        val binding = binding ?: return
        binding.slider.bindBubble(binding.sliderBubble)
    }

    private fun initRecyclerView() {
        val binding = binding ?: return
        val context = context ?: return
        binding.rvBeautify.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvBeautify.adapter = beautifyAdapter
    }

    private fun initListeners() {
        val binding = binding ?: return
        binding.slider.setOnProgressChangeListener(object : BidirectionalSlider.OnProgressChangeListener {
            override fun onStartTrackingTouch() {}

            override fun onStopTrackingTouch() {}

            override fun onProgressChanged(progress: Float, fromUser: Boolean) {
            }
        })
    }

    private fun initCollectors() {
        viewModel.itemList.collectIn(lifecycleScope) {
            beautifyAdapter.setItems(it)
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
                binding.vSliderGradient.isInvisible = false
                binding.slider.isInvisible = false
            }
        }
        binding.slider.setValue(0f)
    }
}