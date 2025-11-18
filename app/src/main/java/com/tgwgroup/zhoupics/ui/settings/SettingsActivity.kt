package com.tgwgroup.zhoupics.ui.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.tgwgroup.zhoupics.R
import com.tgwgroup.zhoupics.base.BaseActivity
import com.tgwgroup.zhoupics.databinding.ActivitySettingsBinding
import com.tgwgroup.zhoupics.ui.dev.DeveloperActivity
import com.tgwgroup.zhoupics.ui.downloads.DownloadsActivity
import com.tgwgroup.zhoupics.ui.settings.language.LanguageSelectBottomSheet

class SettingsActivity : BaseActivity<ActivitySettingsBinding>() {
    
    private val settingsAdapter = SettingsAdapter()
    
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, SettingsActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onBindingCreate(): ActivitySettingsBinding {
        return ActivitySettingsBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        binding.ivBack.setOnClickListener {
            finish()
        }
        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            val versionName = packageInfo.versionName
            binding.tvVersion.text = getString(R.string.version_x, versionName)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        initRecyclerView()
    }
    
    private fun initRecyclerView() {
        binding.rvItems.layoutManager = LinearLayoutManager(this)
        binding.rvItems.adapter = settingsAdapter
        
        val items = listOf(
            SettingsItem(
                icon = R.drawable.ic_language,
                title = getString(R.string.language),
                onClick = {
                    LanguageSelectBottomSheet.show(supportFragmentManager)
                }
            ),
            SettingsItem(
                icon = R.drawable.ic_export,
                title = getString(R.string.downloads),
                onClick = {
                    DownloadsActivity.start(this)
                }
            ),
            SettingsItem(
                icon = R.drawable.ic_developer,
                title = getString(R.string.developer),
                onClick = {
                    DeveloperActivity.start(this)
                }
            )
        )
        settingsAdapter.setItems(items)
    }
}