package com.tgwgroup.zhoupics.ui.gallery

import android.content.Context
import android.content.Intent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tgwgroup.zhoupics.base.BaseActivity
import com.tgwgroup.zhoupics.databinding.ActivityGalleryBinding
import com.tgwgroup.zhoupics.utils.collectIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GalleryActivity : BaseActivity<ActivityGalleryBinding>() {
    private val albumAdapter = AlbumAdapter()

    private val viewModel by viewModels<GalleryViewModel>()

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, GalleryActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onBindingCreate(): ActivityGalleryBinding {
        return ActivityGalleryBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        binding.ivBack.setOnClickListener {
            finish()
        }
        initRecyclerView()
        initCollectors()
    }

    private fun initRecyclerView() {
        binding.rvAlbums.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvAlbums.adapter = albumAdapter

        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.queryAlbums(this@GalleryActivity)
        }
    }

    private fun initCollectors() {
        viewModel.albumList.collectIn(lifecycleScope) {
            albumAdapter.setItems(it)
        }
    }

    override fun isEdgeToEdgeEnabled(): Boolean {
        return false
    }
}