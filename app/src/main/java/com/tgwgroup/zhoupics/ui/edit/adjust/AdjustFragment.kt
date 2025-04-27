package com.tgwgroup.zhoupics.ui.edit.adjust

import androidx.core.view.isInvisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tgwgroup.zhoupics.base.BaseFragment
import com.tgwgroup.zhoupics.databinding.FragmentAdjustBinding
import com.tgwgroup.zhoupics.utils.LogUtil
import com.tgwgroup.zhoupics.utils.collectIn
import com.tgwgroup.zhoupics.widgets.BidirectionalSlider

class AdjustFragment : BaseFragment<FragmentAdjustBinding>() {
    private val adjustAdapter = AdjustAdapter()

    private val viewModel by viewModels<AdjustViewModel>()

    companion object {
        const val TAG = "AdjustTabFragment"
    }

    override fun onBindingCreate(): FragmentAdjustBinding {
        return FragmentAdjustBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        initRecyclerView()
        initListeners()
        initCollectors()
    }

    private fun initRecyclerView() {
        val binding = binding ?: return
        val context = context ?: return
        binding.rvAdjust.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvAdjust.adapter = adjustAdapter
    }

    private fun initListeners() {
        val binding = binding ?: return
        binding.slider.setOnProgressChangeListener(object : BidirectionalSlider.OnProgressChangeListener {
            override fun onStartTrackingTouch() {}

            override fun onStopTrackingTouch() {}

            override fun onProgressChanged(progress: Float, fromUser: Boolean) {
                LogUtil.d(TAG, "progress=$progress, fromUser=$fromUser")
            }
        })
    }

    private fun initCollectors() {
        viewModel.itemList.collectIn(lifecycleScope) {
            adjustAdapter.setItems(it)
        }

        viewModel.selectedItemId.collectIn(lifecycleScope) {
            onSelectedItemChanged(it)
        }
    }

    private fun onSelectedItemChanged(itemId: Int?) {
        val binding = binding ?: return
        val item = viewModel.itemList.value.find { it.id == itemId }
        item?.let {
            if (item.twoWaySlider) {
                binding.slider.setValueRange(-100f, 100f)
                binding.slider.setBidirectional(true)
            } else {
                binding.slider.setValueRange(0f, 100f)
                binding.slider.setBidirectional(false)
            }
            binding.slider.post {
                binding.vSliderGradient.isInvisible = false
                binding.slider.isInvisible = false
            }
        }
        binding.slider.setValue(0f)
    }
}