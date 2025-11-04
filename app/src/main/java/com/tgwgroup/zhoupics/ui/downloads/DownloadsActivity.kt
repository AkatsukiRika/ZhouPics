package com.tgwgroup.zhoupics.ui.downloads

import android.content.Context
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tgwgroup.baselib.utils.LogUtil
import com.tgwgroup.zhoupics.R
import com.tgwgroup.zhoupics.base.BaseActivity
import com.tgwgroup.zhoupics.constants.ELIMINATE_MODEL_NAME
import com.tgwgroup.zhoupics.databinding.ActivityDownloadsBinding
import com.tgwgroup.zhoupics.recyclerview.VerticalSpaceItemDecoration
import com.tgwgroup.zhoupics.utils.DownloadCallback
import com.tgwgroup.zhoupics.utils.downloadFile
import kotlinx.coroutines.Dispatchers
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

        lifecycleScope.launch(Dispatchers.Main) {
            val items = listOf(
                getEliminateModelItem(::onClickItem).apply {
                    if (hasLocalFile(this@DownloadsActivity)) {
                        downloadStatus = DownloadStatus.COMPLETED
                    }
                }
            )
            downloadsAdapter.setItems(items)
        }
    }

    private fun onClickItem(item: DownloadsItem) {
        when (item.downloadStatus) {
            DownloadStatus.NOT_STARTED, DownloadStatus.FAILED -> {
                val job = lifecycleScope.launch {
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
                    downloadFile(item.getUrl(), item.getOutputFile(this@DownloadsActivity), callback)
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