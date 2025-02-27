package com.mmt.extractor.ui

import android.os.Bundle
import com.mmt.extractor.base.BaseActivity
import com.mmt.extractor.databinding.ActivityTestBinding

class TestActivity : BaseActivity() {
    private lateinit var binding: ActivityTestBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}