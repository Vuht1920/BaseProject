package com.mmt.extractor.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.mmt.extractor.R
import com.mmt.extractor.base.BaseFragment
import com.mmt.extractor.databinding.FragmentHomeBinding
import com.mmt.extractor.domain.model.AppInfo
import com.mmt.extractor.ui.detail.DetailActivity
import com.mmt.extractor.ui.home.adapter.AppInfoAdapter
import com.mmt.extractor.utils.extensions.gone
import com.mmt.extractor.utils.extensions.safeCollect
import com.mmt.extractor.utils.extensions.visible
import dev.androidbroadcast.vbpd.viewBinding
import java.util.Locale

class HomeFragment : BaseFragment(R.layout.fragment_home) {
    private val homeViewModel: HomeViewModel by viewModels()
    private val binding: FragmentHomeBinding by viewBinding(FragmentHomeBinding::bind)
    private val appInfoAdapter by lazy {
        AppInfoAdapter { pair ->
            val extras = FragmentNavigatorExtras(pair.second to DetailActivity.TRANSITION_NAME)
            findNavController().navigate(HomeFragmentDirections.toDetailActivity(), null, extras)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
        initObserver()
    }

    private fun initView() {
        binding.rvApps.setHasFixedSize(true)
        binding.rvApps.adapter = appInfoAdapter
    }

    private fun initData() {
        homeViewModel.loadData()
    }

    private fun initObserver() {
        safeCollect(homeViewModel.appInfoFlow,
            collect = {
                updateView(it)
            }
        )
    }

    private fun updateView(appInfos: List<AppInfo>) {
        if (appInfos.isEmpty()) {
            binding.emptyAdview.showEmptyAd()
            binding.emptyAdview.visible()
            listOf(binding.ivSearch, binding.ivSort, binding.ivFilter, binding.rvApps).forEach {
                it.gone()
            }
        } else {
            binding.emptyAdview.gone()
            binding.emptyAdview.hideEmptyAd()
            listOf(binding.ivSearch, binding.ivSort, binding.ivFilter, binding.rvApps).forEach {
                it.visible()
            }
            appInfoAdapter.submitList(appInfos)
        }
        if (appInfos.size > 1) {
            binding.tvTotal.text = String.format(Locale.getDefault(), "%d %s", appInfos.size, getString(R.string.txt_apps))
        } else {
            binding.tvTotal.text = String.format(Locale.getDefault(), "%d %s", appInfos.size, getString(R.string.txt_app))
        }
    }
}