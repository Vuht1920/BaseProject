package com.mmt.extractor.ui.detail

import android.os.Bundle
import com.mmt.extractor.R
import com.mmt.extractor.base.BaseActivity
import com.mmt.extractor.databinding.ActivityDetailBinding
import com.mmt.extractor.utils.extensions.animationScroll

class DetailActivity : BaseActivity() {
    companion object {
        const val TRANSITION_NAME = "app_icon"
    }

    private lateinit var binding: ActivityDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        with(binding) {
            toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
            appbarLayout.animationScroll(llHeader, collapsingLayout, getString(R.string.app_name))
        }
    }
}