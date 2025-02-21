package com.mmt.extractor.ui.settings

import com.mmt.extractor.R
import com.mmt.extractor.base.BaseFragment
import com.mmt.extractor.databinding.FragmentSettingsBinding
import dev.androidbroadcast.vbpd.viewBinding

class SettingsFragment : BaseFragment(R.layout.fragment_settings) {
    private val binding: FragmentSettingsBinding by viewBinding(FragmentSettingsBinding::bind)
}