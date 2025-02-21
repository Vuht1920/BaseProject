package com.mmt.extractor.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.mmt.extractor.R
import com.mmt.extractor.base.BaseFragment
import com.mmt.extractor.databinding.FragmentHomeBinding
import com.mmt.extractor.utils.log.DebugLog
import dev.androidbroadcast.vbpd.viewBinding

class HomeFragment : BaseFragment(R.layout.fragment_home) {
    private val homeViewModel: HomeViewModel by viewModels()
    private val binding: FragmentHomeBinding by viewBinding(FragmentHomeBinding::bind)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        DebugLog.loge("dlaldsadl: ")
        homeViewModel.loadData()
    }
}