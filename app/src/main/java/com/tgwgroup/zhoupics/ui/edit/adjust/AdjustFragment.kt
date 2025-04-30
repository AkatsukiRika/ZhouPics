package com.tgwgroup.zhoupics.ui.edit.adjust

import androidx.core.view.isInvisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tgwgroup.zhoupics.base.BaseFragment
import com.tgwgroup.zhoupics.databinding.FragmentAdjustBinding
import com.tgwgroup.zhoupics.history.AdjustRecord
import com.tgwgroup.zhoupics.ui.edit.EditActivity
import com.tgwgroup.zhoupics.ui.edit.EditViewModel
import com.tgwgroup.zhoupics.utils.collectIn
import com.tgwgroup.zhoupics.utils.dpToPx
import com.tgwgroup.zhoupics.widgets.BidirectionalSlider

class AdjustFragment : BaseFragment<FragmentAdjustBinding>() {
    private val adjustAdapter = AdjustAdapter()

    private val viewModel by viewModels<AdjustViewModel>()

    private val editViewModel by activityViewModels<EditViewModel>()

    companion object {
        const val TAG = "AdjustTabFragment"
    }

    override fun onBindingCreate(): FragmentAdjustBinding {
        return FragmentAdjustBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        val latestRecord = editViewModel.historyHelper.getLatestRecord(AdjustRecord::class.java) as? AdjustRecord
        viewModel.init(latestRecord)
        initRecyclerView()
        initListeners()
        initCollectors()

        val binding = binding ?: return
        binding.slider.bindBubble(binding.sliderBubble)
        binding.rvAdjust.post {
            getEditActivity()?.tabFragmentBodyHeight?.value = binding.rvAdjust.height + dpToPx(8f)
        }
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

            override fun onStopTrackingTouch() {
                editViewModel.historyHelper.addRecord(viewModel.getHistoryRecord())
            }

            override fun onProgressChanged(progress: Float, fromUser: Boolean) {
                if (!fromUser) {
                    return
                }
                when (viewModel.selectedItemId.value) {
                    ADJUST_CONTRAST -> {
                        viewModel.updateProgress(ADJUST_CONTRAST, progress)
                        getEditActivity()?.renderHelper?.updateContrastProgress(progress)
                    }
                    ADJUST_EXPOSURE -> {
                        viewModel.updateProgress(ADJUST_EXPOSURE, progress)
                        getEditActivity()?.renderHelper?.updateExposureProgress(progress)
                    }
                    ADJUST_SATURATION -> {
                        viewModel.updateProgress(ADJUST_SATURATION, progress)
                        getEditActivity()?.renderHelper?.updateSaturationProgress(progress)
                    }
                    ADJUST_SHARPNESS -> {
                        viewModel.updateProgress(ADJUST_SHARPNESS, progress)
                        getEditActivity()?.renderHelper?.updateSharpnessProgress(progress)
                    }
                    ADJUST_BRIGHTNESS -> {
                        viewModel.updateProgress(ADJUST_BRIGHTNESS, progress)
                        getEditActivity()?.renderHelper?.updateBrightnessProgress(progress)
                    }
                }
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
                if (binding.slider.isInvisible) {
                    binding.vSliderGradient.isInvisible = false
                    binding.slider.isInvisible = false
                    getEditActivity()?.tabFragmentBodyHeight?.value = binding.rvAdjust.height + binding.slider.height
                }
            }
        }
        binding.slider.setValue(item?.progress ?: 0f)
    }

    private fun getEditActivity(): EditActivity? {
        return activity as? EditActivity
    }
}