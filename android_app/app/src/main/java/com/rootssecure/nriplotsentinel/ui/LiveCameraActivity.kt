package com.rootssecure.nriplotsentinel.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rootssecure.nriplotsentinel.databinding.ActivityLiveCameraBinding

class LiveCameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLiveCameraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiveCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.title = getString(com.rootssecure.nriplotsentinel.R.string.live_camera_feed)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }
}
