package com.rootssecure.sentinel.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.rootssecure.sentinel.domain.repository.DeveloperSettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dev_settings")

@Singleton
class DeveloperSettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : DeveloperSettingsRepository {

    private val KEY_DEV_MODE = booleanPreferencesKey("is_developer_mode_enabled")

    override val isDeveloperModeEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_DEV_MODE] ?: false
    }

    override suspend fun setDeveloperMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_DEV_MODE] = enabled
        }
    }
}
