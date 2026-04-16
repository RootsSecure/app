package com.rootssecure.sentinel.domain.repository

import kotlinx.coroutines.flow.Flow

interface DeveloperSettingsRepository {
    val isDeveloperModeEnabled: Flow<Boolean>
    suspend fun setDeveloperMode(enabled: Boolean)
}
