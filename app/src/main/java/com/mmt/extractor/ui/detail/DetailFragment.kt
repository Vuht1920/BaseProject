package com.mmt.extractor.ui.detail

import android.os.Bundle
import android.view.View
import com.mmt.extractor.R
import com.mmt.extractor.base.BaseFragment
import com.mmt.extractor.databinding.FragmentDetailBinding
import dev.androidbroadcast.vbpd.viewBinding

class DetailFragment : BaseFragment(R.layout.fragment_detail) {
    val binding by viewBinding(FragmentDetailBinding::bind)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}