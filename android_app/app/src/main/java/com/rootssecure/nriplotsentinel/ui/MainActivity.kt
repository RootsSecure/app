package com.rootssecure.nriplotsentinel.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.rootssecure.nriplotsentinel.R
import com.rootssecure.nriplotsentinel.api.ApiClient
import com.rootssecure.nriplotsentinel.databinding.ActivityMainBinding
import com.rootssecure.nriplotsentinel.repository.SentinelRepository
import com.rootssecure.nriplotsentinel.viewmodel.DashboardViewModel
import com.rootssecure.nriplotsentinel.viewmodel.DashboardViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: DashboardViewModel by viewModels {
        DashboardViewModelFactory(
            SentinelRepository(ApiClient.service)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUi()
        observeViewModel()
        viewModel.refresh()
    }

    private fun setupUi() {
        binding.toolbar.title = getString(R.string.dashboard_title)
        binding.toolbar.subtitle = getString(R.string.dashboard_subtitle)

        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }
        binding.retryButton.setOnClickListener { viewModel.refresh() }

        binding.eventHistoryButton.setOnClickListener {
            startActivity(Intent(this, EventHistoryActivity::class.java))
        }

        binding.liveCameraButton.setOnClickListener {
            startActivity(Intent(this, LiveCameraActivity::class.java))
        }
    }

    private fun observeViewModel() {
        viewModel.loading.observe(this) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
            binding.loadingBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.dashboardState.observe(this) { state ->
            binding.deviceNameValue.text = state.deviceName
            binding.deviceStatusValue.text = if (state.isOnline) {
                getString(R.string.status_online)
            } else {
                getString(R.string.status_offline)
            }
            binding.deviceStatusBadge.text = if (state.isOnline) {
                getString(R.string.online_badge)
            } else {
                getString(R.string.offline_badge)
            }
            binding.lastHeartbeatValue.text = state.lastHeartbeat
            binding.networkStatusValue.text = state.networkStatus
            binding.batteryValue.text = state.batteryLabel
            binding.errorGroup.visibility = View.GONE
        }

        viewModel.error.observe(this) { message ->
            val hasError = !message.isNullOrBlank()
            binding.errorGroup.visibility = if (hasError) View.VISIBLE else View.GONE
            binding.errorText.text = message.orEmpty()
            if (hasError) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
