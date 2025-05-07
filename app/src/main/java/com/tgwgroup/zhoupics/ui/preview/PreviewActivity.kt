package com.tgwgroup.zhoupics.ui.preview

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.lifecycle.lifecycleScope
import com.davemorrissey.labs.subscaleview.ImageSource
import com.tgwgroup.zhoupics.base.BaseActivity
import com.tgwgroup.zhoupics.databinding.ActivityPreviewBinding
import com.tgwgroup.zhoupics.ui.edit.EditActivity
import com.tgwgroup.zhoupics.utils.getBitmap
import com.tgwgroup.zhoupics.utils.getInfoStringFromUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PreviewActivity : BaseActivity<ActivityPreviewBinding>() {
    private var uri: Uri? = null

    companion object {
        private const val EXTRA_URI = "uri"

        fun start(context: Context, uri: Uri) {
            val intent = Intent(context, PreviewActivity::class.java).apply {
                putExtra(EXTRA_URI, uri)
            }
            context.startActivity(intent)
        }
    }

    override fun onBindingCreate(): ActivityPreviewBinding {
        return ActivityPreviewBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(EXTRA_URI, Uri::class.java)
        } else {
            intent.getParcelableExtra(EXTRA_URI)
        }
        uri?.let { uri ->
            lifecycleScope.launch(Dispatchers.IO) {
                val bitmap = getBitmap(uri)
                withContext(Dispatchers.Main) {
                    bitmap?.let {
                        binding.ivPreview.setImage(ImageSource.bitmap(it))
                    }
                }
            }
            binding.tvEdit.setOnClickListener {
                EditActivity.start(this, uri)
            }
            binding.tvInfo.text = getInfoStringFromUri(uri)
        }

        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    override fun isEdgeToEdgeEnabled(): Boolean {
        return false
    }
}