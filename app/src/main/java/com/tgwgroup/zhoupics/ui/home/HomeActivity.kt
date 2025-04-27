package com.tgwgroup.zhoupics.ui.home

import android.net.Uri
import android.os.Build
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.tgwgroup.zhoupics.base.BaseActivity
import com.tgwgroup.zhoupics.databinding.ActivityHomeBinding
import com.tgwgroup.zhoupics.ui.edit.EditActivity
import com.tgwgroup.zhoupics.ui.settings.SettingsActivity
import com.tgwgroup.zhoupics.utils.LogUtil

class HomeActivity : BaseActivity<ActivityHomeBinding>() {
    companion object {
        const val TAG = "HomeActivity"
    }

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            handleSelectedImage(uri)
        }
    }

    private val pickImageLegacy = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            handleSelectedImage(uri)
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

    private fun launchPhotoPicker() {
        // 对于 Android 13 (API 33) 及更高版本，且支持新的 Photo Picker
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && ActivityResultContracts.PickVisualMedia.isPhotoPickerAvailable(this)) {
            // 使用新的 Photo Picker API
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            // 使用旧版 API
            pickImageLegacy.launch("image/*")
        }
    }

    private fun handleSelectedImage(uri: Uri) {
        LogUtil.d(TAG, "Selected image URI: $uri")
        EditActivity.start(this, uri)
    }
}