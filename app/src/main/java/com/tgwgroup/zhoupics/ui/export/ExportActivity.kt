package com.tgwgroup.zhoupics.ui.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.davemorrissey.labs.subscaleview.ImageSource
import com.tgwgroup.zhoupics.base.BaseActivity
import com.tgwgroup.zhoupics.constants.EXTRA_URI
import com.tgwgroup.zhoupics.databinding.ActivityExportBinding
import com.tgwgroup.zhoupics.ui.home.HomeActivity
import com.tgwgroup.zhoupics.utils.getParcelableExtraCompat
import com.tgwgroup.zhoupics.utils.preloadAlbumList

class ExportActivity : BaseActivity<ActivityExportBinding>() {
    private var uri: Uri? = null

    companion object {
        fun start(context: Context, uri: Uri) {
            val intent = Intent(context, ExportActivity::class.java).apply {
                putExtra(EXTRA_URI, uri)
            }
            context.startActivity(intent)
        }
    }

    override fun onBindingCreate(): ActivityExportBinding {
        return ActivityExportBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        uri = intent?.getParcelableExtraCompat(EXTRA_URI, Uri::class.java)
        uri?.let {
            binding.ivPreview.setImage(ImageSource.uri(it))
        }
        preloadAlbumList()
        binding.llSelectPhoto.setOnClickListener {
            HomeActivity.start(this)
        }
        binding.ivBack.setOnClickListener {
            finish()
        }
    }
}