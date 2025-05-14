package com.tgwgroup.zhoupics.ui.home

import android.content.Context
import android.content.Intent
import com.tgwgroup.zhoupics.base.BaseActivity
import com.tgwgroup.zhoupics.databinding.ActivityHomeBinding
import com.tgwgroup.zhoupics.ui.gallery.GalleryActivity
import com.tgwgroup.zhoupics.ui.settings.SettingsActivity
import com.tgwgroup.zhoupics.utils.handlePermissionsResult
import com.tgwgroup.zhoupics.utils.hasReadStoragePermission
import com.tgwgroup.zhoupics.utils.preloadAlbumList
import com.tgwgroup.zhoupics.utils.requestReadStoragePermission

class HomeActivity : BaseActivity<ActivityHomeBinding>() {
    companion object {
        const val TAG = "HomeActivity"

        fun start(context: Context) {
            val intent = Intent(context, HomeActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            context.startActivity(intent)
        }
    }

    override fun onBindingCreate(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        binding.llSelectPhoto.setOnClickListener {
            launchPhotoPicker()
        }
        binding.flSettings.setOnClickListener {
            SettingsActivity.start(this)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        handlePermissionsResult(
            requestCode,
            permissions,
            grantResults,
            onGranted = {
                preloadAlbumList()
                launchPhotoPicker()
            },
            onDenied = {}
        )
    }

    private fun launchPhotoPicker() {
        if (hasReadStoragePermission(this)) {
            GalleryActivity.start(this)
        } else {
            requestReadStoragePermission(this)
        }
    }
}