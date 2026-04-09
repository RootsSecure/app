package com.rootssecure.nriplotsentinel.repository

import com.rootssecure.nriplotsentinel.api.ApiService
import com.rootssecure.nriplotsentinel.api.EventHistoryItem

class EventHistoryRepository(
    private val apiService: ApiService
) {
    suspend fun fetchEvents(): List<EventHistoryItem> {
        return apiService.getEvents().events
    }
}
