package com.cricpeak.scorelive.billing

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "premium_prefs")

class PremiumManager(private val context: Context) {

    companion object {
        private val IS_PREMIUM = booleanPreferencesKey("is_premium")
    }

    val isPremium: Flow<Boolean> = context.dataStore.data
        .map { it[IS_PREMIUM] ?: false }

    suspend fun setPremiumStatus(value: Boolean) {
        context.dataStore.edit {
            it[IS_PREMIUM] = value
        }
    }
}