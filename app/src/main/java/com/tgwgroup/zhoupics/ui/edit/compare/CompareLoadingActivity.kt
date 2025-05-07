package com.tgwgroup.zhoupics.ui.edit.compare

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.tgwgroup.zhoupics.base.BaseActivity
import com.tgwgroup.zhoupics.constants.EXTRA_URI
import com.tgwgroup.zhoupics.constants.EXTRA_URI_2
import com.tgwgroup.zhoupics.databinding.ActivityCompareLoadingBinding

class CompareLoadingActivity : BaseActivity<ActivityCompareLoadingBinding>() {
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

    override fun isEdgeToEdgeEnabled(): Boolean {
        return false
    }
}