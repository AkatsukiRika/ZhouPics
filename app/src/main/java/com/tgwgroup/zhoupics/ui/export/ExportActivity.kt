package com.tgwgroup.zhoupics.ui.export

import android.content.Context
import android.content.Intent
import com.tgwgroup.zhoupics.base.BaseActivity
import com.tgwgroup.zhoupics.databinding.ActivityExportBinding

class ExportActivity : BaseActivity<ActivityExportBinding>() {
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, ExportActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onBindingCreate(): ActivityExportBinding {
        return ActivityExportBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        binding.ivBack.setOnClickListener {
            finish()
        }
    }
}