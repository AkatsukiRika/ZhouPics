package com.tgwgroup.zhoupics.ui.loading

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.tgwgroup.zhoupics.base.BaseDialogFragment
import com.tgwgroup.zhoupics.databinding.FragmentLoadingDialogBinding

class LoadingDialogFragment : BaseDialogFragment<FragmentLoadingDialogBinding>() {
    companion object {
        private const val TAG = "LoadingDialogFragment"

        fun show(fragmentManager: FragmentManager) {
            val dialog = LoadingDialogFragment()
            dialog.show(fragmentManager, TAG)
        }

        fun dismiss(fragmentManager: FragmentManager) {
            val dialog = fragmentManager.findFragmentByTag(TAG) as? DialogFragment
            dialog?.dismissAllowingStateLoss()
        }
    }

    override fun onBindingCreate(inflater: LayoutInflater, container: ViewGroup?): FragmentLoadingDialogBinding {
        return FragmentLoadingDialogBinding.inflate(inflater, container, false)
    }

    override fun onStart() {
        super.onStart()
        dialog?.setCancelable(false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}