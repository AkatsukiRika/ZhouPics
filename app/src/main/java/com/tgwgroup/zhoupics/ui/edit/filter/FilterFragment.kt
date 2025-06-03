package com.tgwgroup.zhoupics.ui.edit.filter

import androidx.core.view.isInvisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tgwgroup.zhoupics.base.BaseFragment
import com.tgwgroup.zhoupics.databinding.FragmentFilterBinding
import com.tgwgroup.zhoupics.history.FilterRecord
import com.tgwgroup.zhoupics.history.HistoryRecord
import com.tgwgroup.zhoupics.render.RenderHelper
import com.tgwgroup.zhoupics.ui.edit.EditActivity
import com.tgwgroup.zhoupics.ui.edit.EditViewModel
import com.tgwgroup.zhoupics.utils.collectIn
import com.tgwgroup.zhoupics.utils.dpToPx
import com.tgwgroup.zhoupics.widgets.BidirectionalSlider
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class FilterFragment : BaseFragment<FragmentFilterBinding>() {
    private val filterAdapter = FilterAdapter()

    private val viewModel by viewModels<FilterViewModel>()

    private val editViewModel by activityViewModels<EditViewModel>()

    private val renderScope = MainScope()

    private var addRecord = true

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
        initListeners()
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

    private fun initListeners() {
        val binding = binding ?: return
        binding.slider.setOnProgressChangeListener(object : BidirectionalSlider.OnProgressChangeListener {
            private var lastProgress = 100f

            override fun onStartTrackingTouch() {}

            override fun onStopTrackingTouch() {
                val selectedItemId = viewModel.selectedItemId.value
                selectedItemId?.let {
                    editViewModel.historyHelper.addRecord(FilterRecord(
                        filterType = it,
                        filterProgress = lastProgress
                    ))
                }
            }

            override fun onProgressChanged(progress: Float, fromUser: Boolean) {
                if (!fromUser) {
                    return
                }
                val selectedItemId = viewModel.selectedItemId.value
                if (selectedItemId != null) {
                    getEditActivity()?.renderHelper?.apply {
                        lastProgress = progress
                        updateCustomFilter(selectedItemId, progress)
                        doRender()
                    }
                }
            }
        })
    }

    private fun initCollectors() {
        viewModel.itemList.collectIn(lifecycleScope) {
            filterAdapter.setItems(it)
        }

        viewModel.selectedItemId.collectIn(lifecycleScope) {
            onSelectedItemChanged(it)
        }

        renderScope.launch {
            // Capturing renderHelper in the closure to ensure rendering is still available after fragment being detached.
            val renderHelper = getEditActivity()?.renderHelper

            launch {
                editViewModel.historyHelper.undoEvent.collect {
                    updateProgress(it.receivedRecord, renderHelper)
                }
            }

            launch {
                editViewModel.historyHelper.redoEvent.collect {
                    updateProgress(it.receivedRecord, renderHelper)
                }
            }
        }
    }

    private fun onSelectedItemChanged(itemId: Int?) {
        val binding = binding ?: return
        val item = viewModel.itemList.value.find { it.id == itemId }
        item?.let {
            binding.slider.post {
                if (it.id != FILTER_ORIGINAL && binding.slider.isInvisible) {
                    binding.vSliderGradient.isInvisible = false
                    binding.slider.isInvisible = false
                    getEditActivity()?.tabFragmentSliderHeight?.value = binding.slider.height
                } else if (it.id == FILTER_ORIGINAL) {
                    binding.slider.isInvisible = true
                    binding.vSliderGradient.isInvisible = true
                    getEditActivity()?.tabFragmentSliderHeight?.value = 0
                }
                if (addRecord) {
                    editViewModel.historyHelper.addRecord(FilterRecord(
                        filterType = it.id,
                        filterProgress = it.progress
                    ))
                } else {
                    addRecord = true
                }
                binding.slider.setValue(it.progress)
            }
            getEditActivity()?.renderHelper?.apply {
                updateCustomFilter(type = it.id, progress = it.progress)
                doRender()
            }
        }
    }

    private fun updateProgress(record: HistoryRecord?, renderHelper: RenderHelper?) {
        val binding = binding ?: return
        addRecord = false
        if (record is FilterRecord) {
            viewModel.selectItem(record.filterType)
            binding.slider.setValue(record.filterProgress)
            renderHelper?.apply {
                updateCustomFilter(type = record.filterType, progress = record.filterProgress)
                doRender()
            }
        } else if (editViewModel.historyHelper.isBeforeEarliestRecord(FilterRecord::class.java)) {
            viewModel.selectItem(null)
            binding.slider.isInvisible = true
            renderHelper?.apply {
                updateCustomFilter(type = FILTER_ORIGINAL, progress = 100f)
                doRender()
            }
        }
    }

    private fun getEditActivity(): EditActivity? {
        return activity as? EditActivity
    }

    fun recycle() {
        renderScope.cancel()
    }
}