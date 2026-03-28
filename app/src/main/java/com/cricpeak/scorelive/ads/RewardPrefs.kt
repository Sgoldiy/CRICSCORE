package com.cricpeak.scorelive.ads

import android.content.Context
import androidx.core.content.edit

object RewardPrefs {
    private const val PREFS_NAME = "reward_prefs"
    private const val KEY_POLICY_ACCEPTED = "policy_accepted"
    private const val KEY_AI_UNLOCK_COUNT = "ai_unlock_count"
    private const val KEY_OVERS_UNLOCKED_UNTIL = "overs_unlocked_until"

    private fun getPrefs(context: Context) = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isPolicyAccepted(context: Context): Boolean = 
        getPrefs(context).getBoolean(KEY_POLICY_ACCEPTED, false)

    fun setPolicyAccepted(context: Context, accepted: Boolean) {
        getPrefs(context).edit { putBoolean(KEY_POLICY_ACCEPTED, accepted) }
    }

    // AI Prediction Unlocks
    fun getAiUnlockCount(context: Context): Int = 
        getPrefs(context).getInt(KEY_AI_UNLOCK_COUNT, 0)

    fun useAiUnlock(context: Context) {
        val current = getAiUnlockCount(context)
        if (current > 0) {
            getPrefs(context).edit { putInt(KEY_AI_UNLOCK_COUNT, current - 1) }
        }
    }

    fun addAiUnlocks(context: Context, count: Int = 2) {
        val current = getAiUnlockCount(context)
        getPrefs(context).edit { putInt(KEY_AI_UNLOCK_COUNT, current + count) }
    }

    // Overs Unlocks
    fun getOversUnlockedUntil(context: Context, matchId: Int): Int = 
        getPrefs(context).getInt("${KEY_OVERS_UNLOCKED_UNTIL}_$matchId", 2) // Default 2 overs free

    fun setOversUnlockedUntil(context: Context, matchId: Int, overNumber: Int) {
        getPrefs(context).edit { putInt("${KEY_OVERS_UNLOCKED_UNTIL}_$matchId", overNumber) }
    }
}
