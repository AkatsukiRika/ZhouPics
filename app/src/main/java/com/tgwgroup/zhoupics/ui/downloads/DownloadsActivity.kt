package com.tgwgroup.zhoupics.ui.downloads

import android.content.Context
import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import com.tgwgroup.zhoupics.R
import com.tgwgroup.zhoupics.base.BaseActivity
import com.tgwgroup.zhoupics.constants.ELIMINATE_MODEL_NAME
import com.tgwgroup.zhoupics.databinding.ActivityDownloadsBinding
import com.tgwgroup.zhoupics.recyclerview.VerticalSpaceItemDecoration

class DownloadsActivity : BaseActivity<ActivityDownloadsBinding>() {
    private val downloadsAdapter = DownloadsAdapter()

    companion object {
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
                title = getString(R.string.elimination_model),
                fileName = ELIMINATE_MODEL_NAME,
                fileSizeBytes = 28265660L,
                onClick = {}
            )
        )
        downloadsAdapter.setItems(items)
    }
}