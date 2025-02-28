package com.mmt.extractor.ui.detail

import android.os.Bundle
import android.transition.TransitionInflater
import android.view.View
import com.mmt.extractor.R
import com.mmt.extractor.base.BaseFragment
import com.mmt.extractor.databinding.ActivityDetailBinding
import com.mmt.extractor.utils.extensions.animationScroll
import dev.androidbroadcast.vbpd.viewBinding

class DetailFragment : BaseFragment(R.layout.activity_detail) {
    private val binding by viewBinding(ActivityDetailBinding::bind)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val animation = TransitionInflater.from(requireContext()).inflateTransition(
            android.R.transition.move
        )
        sharedElementEnterTransition = animation
        sharedElementReturnTransition = animation
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        binding.appbarLayout.animationScroll(binding.llHeader, binding.collapsingLayout, getString(R.string.app_name))
    }
}