package com.rootssecure.nriplotsentinel.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.rootssecure.nriplotsentinel.R
import com.rootssecure.nriplotsentinel.api.ApiClient
import com.rootssecure.nriplotsentinel.databinding.ActivityMainBinding
import com.rootssecure.nriplotsentinel.repository.SentinelRepository
import com.rootssecure.nriplotsentinel.viewmodel.DashboardViewModel
import com.rootssecure.nriplotsentinel.viewmodel.DashboardViewModelFactory

// Stitch UI "Digital Panopticon" Android Wrapper
// Prioritizes launching Native LiveCameraActivity securely and wrapping Kivy logic
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: DashboardViewModel by viewModels {
        DashboardViewModelFactory(
            SentinelRepository(ApiClient.service)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ensure status bar matches the dark Panopticon theme
        window.statusBarColor = android.graphics.Color.parseColor("#131313")
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUi()
        observeViewModel()
        viewModel.refresh()
    }

    private fun setupUi() {
        // Toolbar UI shifted to dark #1c1b1b
        binding.toolbar.setBackgroundColor(android.graphics.Color.parseColor("#1c1b1b"))
        binding.toolbar.setTitleTextColor(android.graphics.Color.parseColor("#e5e2e1"))
        binding.toolbar.title = "Operations Dashboard"

        // Setup the swipe refresh to use Primary Teal (#55d8e1)
        binding.swipeRefresh.setColorSchemeColors(android.graphics.Color.parseColor("#55d8e1"))
        binding.swipeRefresh.setProgressBackgroundColorSchemeColor(android.graphics.Color.parseColor("#313030"))
        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        // Live Camera Wrapper (High Priority Native Logic)
        binding.liveCameraButton.setBackgroundColor(android.graphics.Color.parseColor("#00adb5"))
        binding.liveCameraButton.setTextColor(android.graphics.Color.parseColor("#131313"))
        binding.liveCameraButton.setOnClickListener {
            // Launches native Exoplayer/WebRTC pipeline for zero-latency streams
            startActivity(Intent(this, LiveCameraActivity::class.java))
        }

        binding.eventHistoryButton.setOnClickListener {
            startActivity(Intent(this, EventHistoryActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.loading.observe(this) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
            binding.loadingBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.dashboardState.observe(this) { state ->
            binding.deviceNameValue.text = state.deviceName
            binding.deviceStatusValue.text = if (state.isOnline) "ONLINE" else "OFFLINE"
            
            // Apply Status Pulse colors
            val statusColor = if (state.isOnline) "#55d8e1" else "#ffb4ab"
            binding.deviceStatusValue.setTextColor(android.graphics.Color.parseColor(statusColor))
            
            binding.lastHeartbeatValue.text = state.lastHeartbeat
            binding.errorGroup.visibility = View.GONE
        }
    }
}
