package com.rootssecure.nriplotsentinel.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.authDataStore by preferencesDataStore(name = "auth_prefs")

class TokenDataStore(private val context: Context) {

    companion object {
        private val JWT_TOKEN = stringPreferencesKey("jwt_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }

    val tokenFlow: Flow<String?> = context.authDataStore.data.map { preferences ->
        preferences[JWT_TOKEN]
    }

    suspend fun saveToken(token: String, refreshToken: String?) {
        context.authDataStore.edit { preferences ->
            preferences[JWT_TOKEN] = token
            if (!refreshToken.isNullOrBlank()) {
                preferences[REFRESH_TOKEN] = refreshToken
            }
        }
    }

    suspend fun clear() {
        context.authDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
