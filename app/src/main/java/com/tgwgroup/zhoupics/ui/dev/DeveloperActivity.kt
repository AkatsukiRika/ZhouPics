package com.tgwgroup.zhoupics.ui.dev

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import com.tgwgroup.zhoupics.base.BaseActivity
import com.tgwgroup.zhoupics.databinding.ActivityDeveloperBinding
import com.tgwgroup.zhoupics.utils.cpuCores
import com.tgwgroup.zhoupics.utils.cpuFrequencyMhz
import com.tgwgroup.zhoupics.utils.deviceLevel
import com.tgwgroup.zhoupics.utils.glEsVersionMajor
import com.tgwgroup.zhoupics.utils.glEsVersionMinor
import com.tgwgroup.zhoupics.utils.internalStorageMB
import com.tgwgroup.zhoupics.utils.isLowRamDevice
import com.tgwgroup.zhoupics.utils.maxSize
import com.tgwgroup.zhoupics.utils.totalMemoryMB

class DeveloperActivity : BaseActivity<ActivityDeveloperBinding>() {
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, DeveloperActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onBindingCreate(): ActivityDeveloperBinding {
        return ActivityDeveloperBinding.inflate(layoutInflater)
    }

    @SuppressLint("SetTextI18n")
    override fun initView() {
        super.initView()

        binding.tvDeviceLevelValue.text = deviceLevel.toString()
        binding.tvMaxSizeValue.text = maxSize.toString()
        binding.tvLowRamValue.text = isLowRamDevice.toString()
        binding.tvCpuCoresValue.text = cpuCores.toString()
        binding.tvCpuFreqValue.text = cpuFrequencyMhz.toString()
        binding.tvRamValue.text = totalMemoryMB.toString()
        binding.tvStorageValue.text = internalStorageMB.toString()
        binding.tvGlesVersionValue.text = "${glEsVersionMajor}.${glEsVersionMinor}"
    }
}