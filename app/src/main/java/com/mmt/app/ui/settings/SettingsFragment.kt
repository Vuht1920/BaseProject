package com.mmt.app.ui.settings

import android.os.Build.VERSION_CODES.S
import com.mmt.app.base.BaseFragment
import com.mmt.databinding.FragmentHomeBinding
import com.mmt.databinding.FragmentSettingsBinding
import dev.androidbroadcast.vbpd.viewBinding

class SettingsFragment : BaseFragment() {
    private val binding: FragmentSettingsBinding by viewBinding(FragmentSettingsBinding::bind)
}