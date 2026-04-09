package com.rootssecure.nriplotsentinel.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rootssecure.nriplotsentinel.api.EventHistoryItem
import com.rootssecure.nriplotsentinel.repository.EventHistoryRepository
import kotlinx.coroutines.launch

class EventHistoryViewModel(
    private val repository: EventHistoryRepository
) : ViewModel() {

    private val _events = MutableLiveData<List<EventHistoryItem>>(emptyList())
    val events: LiveData<List<EventHistoryItem>> = _events

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun refresh() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            runCatching { repository.fetchEvents() }
                .onSuccess { _events.value = it }
                .onFailure { _error.value = it.message ?: "Unable to load event history." }
            _loading.value = false
        }
    }
}

class EventHistoryViewModelFactory(
    private val repository: EventHistoryRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EventHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EventHistoryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
