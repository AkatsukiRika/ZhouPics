package com.tgwgroup.zhoupics.ui.edit.compare

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.tgwgroup.facecomparelib.CompareFacesCallback
import com.tgwgroup.facecomparelib.FacePPUtils
import com.tgwgroup.zhoupics.R
import com.tgwgroup.zhoupics.base.BaseActivity
import com.tgwgroup.zhoupics.constants.EXTRA_URI
import com.tgwgroup.zhoupics.constants.EXTRA_URI_2
import com.tgwgroup.zhoupics.databinding.ActivityCompareLoadingBinding
import com.tgwgroup.zhoupics.utils.getBitmap
import com.tgwgroup.zhoupics.utils.getParcelableExtraCompat
import com.tgwgroup.zhoupics.utils.toTimeString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class CompareLoadingActivity : BaseActivity<ActivityCompareLoadingBinding>() {
    private var uri1: Uri? = null

    private var bitmap1: Bitmap? = null

    private var uri2: Uri? = null

    private var bitmap2: Bitmap? = null

    private var timeJob: Job? = null

    companion object {
        const val TAG = "CompareLoadingActivity"

        fun start(context: Context, uri: Uri, uri2: Uri) {
            val intent = Intent(context, CompareLoadingActivity::class.java).apply {
                putExtra(EXTRA_URI, uri)
                putExtra(EXTRA_URI_2, uri2)
            }
            context.startActivity(intent)
        }
    }

    override fun onBindingCreate(): ActivityCompareLoadingBinding {
        return ActivityCompareLoadingBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        uri1 = intent.getParcelableExtraCompat(EXTRA_URI, Uri::class.java)
        uri2 = intent.getParcelableExtraCompat(EXTRA_URI_2, Uri::class.java)
        lifecycleScope.launch {
            bitmap1 = uri1?.let { getBitmap(it) }
            bitmap2 = uri2?.let { getBitmap(it) }
            bitmap1?.let {
                withContext(Dispatchers.Main) {
                    binding.ivImage1.setImageBitmap(it)
                }
            }
            bitmap2?.let {
                withContext(Dispatchers.Main) {
                    binding.ivImage2.setImageBitmap(it)
                }
            }
            doCompare()
        }

        binding.ivClose.setOnClickListener {
            finish()
        }
    }

    private fun doCompare() {
        if (bitmap1 != null && bitmap2 != null) {
            startTimeCounting()
            FacePPUtils.compareFaces(
                bitmap1!!,
                bitmap2!!,
                object : CompareFacesCallback {
                    override fun onPrepare() {
                        runOnUiThread {
                            binding.tvDesc.text = getString(R.string.compare_loading_state_1)
                            binding.progressIndicator.setProgress(25, true)
                        }
                    }

                    override fun onRequest() {
                        runOnUiThread {
                            binding.tvDesc.text = getString(R.string.compare_loading_state_2)
                            binding.progressIndicator.setProgress(50, true)
                        }
                    }

                    override fun onResponse() {
                        runOnUiThread {
                            binding.tvDesc.text = getString(R.string.compare_loading_state_3)
                            binding.progressIndicator.setProgress(75, true)
                        }
                    }

                    override fun onSuccess(similarity: Float?) {
                        runOnUiThread {
                            timeJob?.cancel()
                            binding.progressIndicator.setProgress(100, true)
                            if (similarity != null) {
                                binding.tvTime.text = String.format(Locale.ROOT, "%.2f%%", similarity)
                                binding.tvDesc.text = getString(R.string.face_similarity) + " ⬆"
                            } else {
                                binding.tvTime.text = "0.00%"
                                binding.tvDesc.text = getString(R.string.face_not_detected)
                            }
                            binding.tvBottomAction.isVisible = true
                            binding.tvBottomAction.text = getString(R.string.ok)
                            binding.tvBottomAction.setOnClickListener {
                                finish()
                            }
                        }
                    }

                    override fun onError(exception: Exception) {
                        runOnUiThread {
                            timeJob?.cancel()
                            binding.tvTime.text = "❌"
                            binding.tvDesc.text = exception.localizedMessage
                            binding.progressIndicator.setProgress(100, true)
                            binding.tvBottomAction.isVisible = true
                            binding.tvBottomAction.text = getString(R.string.retry)
                            binding.tvBottomAction.setOnClickListener {
                                doCompare()
                            }
                        }
                    }
                }
            )
        } else {
            runOnUiThread {

            }
        }
    }

    private fun startTimeCounting() {
        timeJob?.cancel()
        timeJob = lifecycleScope.launch(Dispatchers.IO) {
            val beginTime = System.currentTimeMillis()
            while (true) {
                withContext(Dispatchers.Main) {
                    binding.tvTime.text = (System.currentTimeMillis() - beginTime).toTimeString()
                }
                delay(10)
            }
        }
    }

    override fun isEdgeToEdgeEnabled(): Boolean {
        return false
    }
}