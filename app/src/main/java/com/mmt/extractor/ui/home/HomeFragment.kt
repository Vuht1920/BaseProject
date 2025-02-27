package com.mmt.extractor.ui.home

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.mmt.extractor.R
import com.mmt.extractor.base.BaseFragment
import com.mmt.extractor.databinding.FragmentHomeBinding
import com.mmt.extractor.domain.model.AppInfo
import com.mmt.extractor.ui.TestActivity
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
        AppInfoAdapter {
            val extras = FragmentNavigatorExtras(it.second to "app_preview11")
//            Navigation.findNavController(it.second).navigate(R.id.toDetailFragment, null, null, extras)
            val intent = Intent(context, TestActivity::class.java)
            val transitionActivityOptions: ActivityOptions = ActivityOptions.makeSceneTransitionAnimation(activity, it.second, "app_preview11")
            requireContext().startActivity(intent, transitionActivityOptions.toBundle())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
        initObserver()
    }

    private fun initView() {
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