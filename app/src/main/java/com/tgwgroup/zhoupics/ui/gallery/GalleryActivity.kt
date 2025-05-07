package com.tgwgroup.zhoupics.ui.gallery

import android.content.Context
import android.content.Intent
import android.util.TypedValue
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.tgwgroup.zhoupics.R
import com.tgwgroup.zhoupics.base.BaseActivity
import com.tgwgroup.zhoupics.databinding.ActivityGalleryBinding
import com.tgwgroup.zhoupics.ui.edit.EditActivity
import com.tgwgroup.zhoupics.ui.loading.LoadingDialogFragment
import com.tgwgroup.zhoupics.ui.preview.PreviewActivity
import com.tgwgroup.zhoupics.utils.collectIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GalleryActivity : BaseActivity<ActivityGalleryBinding>() {
    private var fromType = FROM_TYPE_HOME

    private val albumAdapter = AlbumAdapter()

    private val imagePagerAdapter = ImagePagerAdapter()

    private val viewModel by viewModels<GalleryViewModel>()

    private var imagePagerInited = false

    companion object {
        private const val EXTRA_FROM_TYPE = "from_type"
        const val FROM_TYPE_HOME = 0
        const val FROM_TYPE_COMPARE_FACES = 1

        fun start(context: Context, fromType: Int = FROM_TYPE_HOME) {
            val intent = Intent(context, GalleryActivity::class.java).apply {
                putExtra(EXTRA_FROM_TYPE, fromType)
            }
            context.startActivity(intent)
        }
    }

    override fun onBindingCreate(): ActivityGalleryBinding {
        return ActivityGalleryBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        fromType = intent.getIntExtra(EXTRA_FROM_TYPE, FROM_TYPE_HOME)
        when (fromType) {
            FROM_TYPE_COMPARE_FACES -> {
                binding.tvTitle.text = getString(R.string.select_another_photo)
                binding.tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            }
        }
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
            viewModel.queryAlbums(onClickImage = { event, uri ->
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