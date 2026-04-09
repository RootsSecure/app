package com.rootssecure.nriplotsentinel.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rootssecure.nriplotsentinel.api.DashboardBundle
import com.rootssecure.nriplotsentinel.repository.SentinelRepository
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: SentinelRepository
) : ViewModel() {

    private val _dashboardBundle = MutableLiveData<DashboardBundle>()
    val dashboardBundle: LiveData<DashboardBundle> = _dashboardBundle

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun refresh() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            runCatching { repository.fetchDashboardBundle() }
                .onSuccess { _dashboardBundle.value = it }
                .onFailure { _error.value = it.message ?: "Unable to load dashboard." }
            _loading.value = false
        }
    }
}

class HomeViewModelFactory(
    private val repository: SentinelRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
