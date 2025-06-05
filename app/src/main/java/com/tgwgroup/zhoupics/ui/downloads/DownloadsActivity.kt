package com.tgwgroup.zhoupics.ui.downloads

import android.content.Context
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tgwgroup.baselib.utils.LogUtil
import com.tgwgroup.zhoupics.R
import com.tgwgroup.zhoupics.base.BaseActivity
import com.tgwgroup.zhoupics.constants.ELIMINATE_MODEL_NAME
import com.tgwgroup.zhoupics.constants.HOSTING_BASE_URL
import com.tgwgroup.zhoupics.constants.getModelDir
import com.tgwgroup.zhoupics.databinding.ActivityDownloadsBinding
import com.tgwgroup.zhoupics.recyclerview.VerticalSpaceItemDecoration
import com.tgwgroup.zhoupics.utils.DownloadCallback
import com.tgwgroup.zhoupics.utils.downloadFile
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

class DownloadsActivity : BaseActivity<ActivityDownloadsBinding>() {
    private val downloadsAdapter = DownloadsAdapter()

    private var downloadJobs = mutableSetOf<Job>()

    companion object {
        const val TAG = "DownloadsActivity"

        fun start(context: Context) {
            val intent = Intent(context, DownloadsActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onBindingCreate(): ActivityDownloadsBinding {
        return ActivityDownloadsBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        binding.ivBack.setOnClickListener {
            finish()
        }
        initRecyclerView()
    }

    private fun initRecyclerView() {
        binding.rvItems.apply {
            layoutManager = LinearLayoutManager(this@DownloadsActivity)
            adapter = downloadsAdapter
            addItemDecoration(VerticalSpaceItemDecoration(verticalSpaceDp = 8))
        }

        val items = listOf(
            DownloadsItem(
                index = 0,
                title = getString(R.string.elimination_model),
                fileName = ELIMINATE_MODEL_NAME,
                fileSizeBytes = 28265660L,
                onClick = ::onClickItem
            ).apply {
            }
        )
        downloadsAdapter.setItems(items)
    }

    private fun onClickItem(item: DownloadsItem) {
        when (item.downloadStatus) {
            DownloadStatus.NOT_STARTED, DownloadStatus.FAILED -> {
                val job = lifecycleScope.launch {
                    val url = "${HOSTING_BASE_URL}${ELIMINATE_MODEL_NAME}"
                    val outputFile = File(getModelDir(this@DownloadsActivity), ELIMINATE_MODEL_NAME)
                    val callback = object : DownloadCallback {
                        override fun onProgress(totalBytesRead: Long) {
                            LogUtil.i(TAG, "item: $item, totalBytesRead: $totalBytesRead")
                            setTotalBytesRead(item, totalBytesRead)
                        }

                        override fun onSuccess(file: File) {
                            LogUtil.i(TAG, "item: $item, onSuccess: ${file.absolutePath}")
                            setDownloadStatus(item, DownloadStatus.COMPLETED)
                        }

                        override fun onFailure(e: Exception) {
                            LogUtil.e(TAG, "item: $item, onFailure", e)
                            setDownloadStatus(item, DownloadStatus.FAILED)
                        }
                    }
                    downloadFile(url, outputFile, callback)
                }
                job.start()
                downloadJobs.add(job)
                setDownloadStatus(item, DownloadStatus.IN_PROGRESS)
            }

            else -> {}
        }
    }

    private fun setTotalBytesRead(item: DownloadsItem, totalBytesRead: Long) {
        item.totalBytesRead = totalBytesRead
        downloadsAdapter.notifyItemChanged(item.index, 0)
    }

    private fun setDownloadStatus(item: DownloadsItem, status: DownloadStatus) {
        item.downloadStatus = status
        downloadsAdapter.notifyItemChanged(item.index, 0)
    }
}