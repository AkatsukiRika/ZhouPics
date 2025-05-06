package com.tgwgroup.zhoupics.ui.gallery

import android.content.Context
import android.content.Intent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.tgwgroup.zhoupics.base.BaseActivity
import com.tgwgroup.zhoupics.databinding.ActivityGalleryBinding
import com.tgwgroup.zhoupics.ui.edit.EditActivity
import com.tgwgroup.zhoupics.ui.loading.LoadingDialogFragment
import com.tgwgroup.zhoupics.ui.preview.PreviewActivity
import com.tgwgroup.zhoupics.utils.collectIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GalleryActivity : BaseActivity<ActivityGalleryBinding>() {
    private val albumAdapter = AlbumAdapter()

    private val imagePagerAdapter = ImagePagerAdapter()

    private val viewModel by viewModels<GalleryViewModel>()

    private var imagePagerInited = false

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

        binding.vpGallery.adapter = imagePagerAdapter
        binding.vpGallery.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                viewModel.selectAlbumByIndex(position)
                binding.rvAlbums.smoothScrollToPosition(position)
            }
        })

        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.queryAlbums(this@GalleryActivity, onClickImage = { event, uri ->
                if (event == ImageClickEvent.GO_EDIT) {
                    EditActivity.start(this@GalleryActivity, uri)
                } else {
                    PreviewActivity.start(this@GalleryActivity, uri)
                }
            })
        }
    }

    private fun initCollectors() {
        viewModel.albumList.collectIn(lifecycleScope) {
            albumAdapter.setItems(it)
            if (!imagePagerInited && it.isNotEmpty()) {
                imagePagerAdapter.setItems(it)
                imagePagerInited = true
            }
            val selectedAlbumIndex = it.indexOfFirst { albumItem -> albumItem.selected }
            binding.vpGallery.setCurrentItem(selectedAlbumIndex, true)
        }
        viewModel.loading.collectIn(lifecycleScope) {
            if (it) {
                LoadingDialogFragment.show(supportFragmentManager)
            } else {
                LoadingDialogFragment.dismiss(supportFragmentManager)
            }
        }
    }

    override fun isEdgeToEdgeEnabled(): Boolean {
        return false
    }
}