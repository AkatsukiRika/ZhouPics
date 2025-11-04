package com.tgwgroup.zhoupics.widgets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.tgwgroup.zhoupics.R
import com.tgwgroup.zhoupics.base.BaseDialogFragment
import com.tgwgroup.zhoupics.databinding.FragmentModelNotReadyDialogBinding
import com.tgwgroup.zhoupics.ui.downloads.DownloadsActivity

class ModelNotReadyDialogFragment : BaseDialogFragment<FragmentModelNotReadyDialogBinding>() {
    companion object {
        private const val TAG = "ModelNotReadyDialogFragment"

        fun show(fragmentManager: FragmentManager, modelName: String) {
            val dialog = ModelNotReadyDialogFragment().apply {
                this.modelName = modelName
            }
            dialog.show(fragmentManager, TAG)
        }

        fun dismiss(fragmentManager: FragmentManager) {
            val dialog = fragmentManager.findFragmentByTag(TAG) as? DialogFragment
            dialog?.dismissAllowingStateLoss()
        }
    }

    var modelName: String = ""

    override fun onBindingCreate(inflater: LayoutInflater, container: ViewGroup?): FragmentModelNotReadyDialogBinding {
        return FragmentModelNotReadyDialogBinding.inflate(inflater, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        val binding = binding ?: return
        binding.tvTips.text = getString(R.string.model_not_ready_tips, modelName)
        binding.tvDownloadNow.setOnClickListener {
            val context = context ?: return@setOnClickListener
            DownloadsActivity.start(context)
        }
    }
}