package com.tgwgroup.zhoupics.ui.edit.elimination

import android.graphics.Matrix
import androidx.core.view.isInvisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.tgwgroup.zhoupics.base.BaseFragment
import com.tgwgroup.zhoupics.databinding.FragmentEliminationBinding
import com.tgwgroup.zhoupics.ui.edit.EditActivity
import com.tgwgroup.zhoupics.ui.edit.EditViewModel
import com.tgwgroup.zhoupics.utils.collectIn
import com.tgwgroup.zhoupics.widgets.BidirectionalSlider

class EliminationFragment : BaseFragment<FragmentEliminationBinding>() {
    private val editViewModel by activityViewModels<EditViewModel>()

    private val viewModel by viewModels<EliminationViewModel>()

    private var isInit = false

    private val currentImageMatrix = Matrix()

    companion object {
        const val TAG = "EliminationFragment"
    }

    override fun onBindingCreate(): FragmentEliminationBinding {
        return FragmentEliminationBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        editViewModel.updateInRoom(true)

        val binding = binding ?: return
        binding.slider.bindBubble(binding.sliderBubble)
        binding.slider.setValueRange(1f, 100f)
        binding.slider.setBidirectional(false)

        initPaintView()
        initTriggers()
        initCollectors()
        initListeners()

        getEditActivity()?.currentBitmap?.let {
            binding.ivElimination.setImage(ImageSource.bitmap(it))
        }
        binding.ivElimination.setOnStateChangedListener(object : SubsamplingScaleImageView.DefaultOnStateChangedListener() {
            override fun onMatrixChanged(matrix: Matrix?) {
                super.onMatrixChanged(matrix)
                matrix?.let {
                    binding.vEliminatePaint.setImageMatrix(matrix, isInit)
                    currentImageMatrix.set(matrix)
                }
            }
        })
    }

    private fun initPaintView() {
        val binding = binding ?: return
        val currentBitmap = getEditActivity()?.currentBitmap ?: return
        binding.vEliminatePaint.apply {
            setMagnifier(binding.vEliminateZoom)
            setOuterView(binding.ivElimination, currentBitmap)
            setDisableTouch(false)
            isInit = true
            setImageMatrix(currentImageMatrix, isInit)
            setCallback(object : EliminatePaintView.Callback {
                override fun onActionUpOrCancel() {}

                override fun onTouchEvent(touchX: Float, touchY: Float) {}
            })
        }
    }

    private fun initTriggers() {
        val binding = binding ?: return
        binding.ivCancel.setOnClickListener {
            finishFragment()
        }
        binding.ivConfirm.setOnClickListener {
            finishFragment()
        }
        binding.llPaint.setOnClickListener {
            viewModel.updateCurrentMode(EliminationViewModel.Mode.PAINT)
        }
        binding.llLariat.setOnClickListener {
            viewModel.updateCurrentMode(EliminationViewModel.Mode.LARIAT)
        }
        binding.llEraser.setOnClickListener {
            viewModel.updateCurrentMode(EliminationViewModel.Mode.ERASER)
        }
    }

    private fun initCollectors() {
        val binding = binding ?: return
        viewModel.currentMode.collectIn(lifecycleScope) { mode ->
            binding.llPaint.isSelected = mode == EliminationViewModel.Mode.PAINT
            binding.llLariat.isSelected = mode == EliminationViewModel.Mode.LARIAT
            binding.llEraser.isSelected = mode == EliminationViewModel.Mode.ERASER
            when (mode) {
                EliminationViewModel.Mode.PAINT -> {
                    binding.slider.post {
                        showSlider()
                        binding.slider.setValue(viewModel.paintSize.value)
                    }
                    binding.vEliminatePaint.apply {
                        endRestore()
                        showIndicator(true)
                        setBrushSize(viewModel.paintSize.value, false)
                        setErase(false)
                    }
                }

                EliminationViewModel.Mode.LARIAT -> {
                    binding.slider.post {
                        hideSlider()
                    }
                    binding.vEliminatePaint.apply {
                        endRestore()
                        showIndicator(true)
                        setDashedLine()
                        setErase(false)
                    }
                }

                EliminationViewModel.Mode.ERASER -> {
                    binding.slider.post {
                        showSlider()
                        binding.slider.setValue(viewModel.eraserSize.value)
                    }
                    binding.vEliminatePaint.setErase(true)
                }
            }
        }
        viewModel.paintSize.collectIn(lifecycleScope) {
            if (viewModel.currentMode.value == EliminationViewModel.Mode.PAINT) {
                binding.vEliminatePaint.setBrushSize(it / 100f)
            }
        }
        viewModel.eraserSize.collectIn(lifecycleScope) {
            if (viewModel.currentMode.value == EliminationViewModel.Mode.ERASER) {
                binding.vEliminatePaint.setBrushSize(it / 100f)
            }
        }
    }

    private fun initListeners() {
        val binding = binding ?: return
        binding.slider.setOnProgressChangeListener(object : BidirectionalSlider.OnProgressChangeListener {
            override fun onStartTrackingTouch() {}

            override fun onStopTrackingTouch() {}

            override fun onProgressChanged(progress: Float, fromUser: Boolean) {
                if (!fromUser) {
                    return
                }
                when (viewModel.currentMode.value) {
                    EliminationViewModel.Mode.PAINT -> {
                        viewModel.updatePaintSize(progress)
                    }
                    EliminationViewModel.Mode.ERASER -> {
                        viewModel.updateEraserSize(progress)
                    }
                    else -> {}
                }
            }
        })
    }

    private fun showSlider() {
        val binding = binding ?: return
        binding.slider.isInvisible = false
        binding.vSliderGradient.isInvisible = false
    }

    private fun hideSlider() {
        val binding = binding ?: return
        binding.slider.isInvisible = true
        binding.vSliderGradient.isInvisible = true
    }

    private fun finishFragment() {
        editViewModel.updateInRoom(false)
        activity?.supportFragmentManager?.beginTransaction()
            ?.remove(this)
            ?.commitNowAllowingStateLoss()
    }

    private fun getEditActivity(): EditActivity? {
        return activity as? EditActivity
    }
}