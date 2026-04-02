package com.cricpeak.scorelive.billing

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "premium_prefs")

class PremiumManager(private val context: Context) {
    companion object {
        private val IS_PREMIUM = booleanPreferencesKey("is_premium")
    }

    val isPremium: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_PREMIUM] ?: false
        }

    suspend fun setPremiumStatus(isPremium: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_PREMIUM] = isPremium
        }
    }
}
