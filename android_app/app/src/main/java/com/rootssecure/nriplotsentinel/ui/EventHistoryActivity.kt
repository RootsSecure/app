package com.rootssecure.nriplotsentinel.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.rootssecure.nriplotsentinel.R
import com.rootssecure.nriplotsentinel.api.ApiClient
import com.rootssecure.nriplotsentinel.databinding.ActivityEventHistoryBinding
import com.rootssecure.nriplotsentinel.repository.EventHistoryRepository
import com.rootssecure.nriplotsentinel.viewmodel.EventHistoryViewModel
import com.rootssecure.nriplotsentinel.viewmodel.EventHistoryViewModelFactory

class EventHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEventHistoryBinding
    private val eventAdapter = EventHistoryAdapter()

    private val viewModel: EventHistoryViewModel by viewModels {
        EventHistoryViewModelFactory(
            EventHistoryRepository(ApiClient.service)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUi()
        observeViewModel()
        viewModel.refresh()
    }

    private fun setupUi() {
        binding.toolbar.title = getString(R.string.event_history)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }
        binding.retryButton.setOnClickListener { viewModel.refresh() }

        binding.eventRecycler.apply {
            layoutManager = LinearLayoutManager(this@EventHistoryActivity)
            adapter = eventAdapter
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        viewModel.loading.observe(this) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
            binding.loadingBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.events.observe(this) { events ->
            eventAdapter.submitList(events)
            binding.emptyText.visibility = if (events.isEmpty()) View.VISIBLE else View.GONE
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
