package com.cricpeak.scorelive.ui.apicallfiles

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.firebase.database.FirebaseDatabase
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

@Serializable
data class GeminiPredictionResult(
    val homeWinProb: Int,
    val awayWinProb: Int,
    val analysis: String,
    val keyPlayers: List<String> = emptyList(),
    val homePlayers: List<String> = emptyList(),
    val awayPlayers: List<String> = emptyList(),
    val predictedWinner: String
)

@Serializable
data class FantasyTeamResult(
    val team: List<String> // 11 player names
)

class GeminiPredictionService {

    // Fallback values in case Firebase fetch fails
    private var currentApiKey = "AIzaSyAfuuSlQKXo8GL0YqcOhQwVAXmydUDpnBE"
    private var currentModelName = "gemini-1.5-flash" // Use a stable version by default

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    /**
     * Fetches latest config from Firebase Realtime Database
     */
    private suspend fun updateConfigFromFirebase() {
        try {
            val database = FirebaseDatabase.getInstance().getReference("gemini_config")
            val snapshot = database.get().await()
            
            val remoteKey = snapshot.child("api_key").value as? String
            val remoteModel = snapshot.child("model_name").value as? String

            if (!remoteKey.isNullOrEmpty()) currentApiKey = remoteKey
            if (!remoteModel.isNullOrEmpty()) currentModelName = remoteModel
            
            Log.d("GeminiService", "Config updated from Firebase: $currentModelName")
        } catch (e: Exception) {
            Log.e("GeminiService", "Firebase Config Error: ${e.message}. Using fallbacks.")
        }
    }

    suspend fun getMatchPrediction(match: MatchDetailsData): Result<GeminiPredictionResult> = withContext(Dispatchers.IO) {
        // 1. Always try to get the latest config first
        updateConfigFromFirebase()

        if (currentApiKey.isEmpty() || currentApiKey.startsWith("YOUR")) {
            return@withContext Result.failure(Exception("API Key not configured."))
        }

        // 2. Initialize model with the fetched config
        val model = GenerativeModel(
            modelName = currentModelName,
            apiKey = currentApiKey,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
            }
        )

        val prompt = """
            Analyze this upcoming cricket match:
            Competition: ${match.competition.name}
            Format: ${match.gameType}
            Match: ${match.homeTeam.name} vs ${match.awayTeam.name}
            Venue: ${match.venue.name}, ${match.venue.city}
            
            Return a JSON object with:
            {
              "homeWinProb": Int,
              "awayWinProb": Int,
              "analysis": "String",
              "homePlayers": ["Name 1", "Name 2", "Name 3", "Name 4", "Name 5"],
              "awayPlayers": ["Name 1", "Name 2", "Name 3", "Name 4", "Name 5"],
              "predictedWinner": "String"
            }
        """.trimIndent()

        try {
            val response = model.generateContent(prompt)
            val responseText = response.text ?: return@withContext Result.failure(Exception("Empty response"))

            val cleanedJson = responseText.trim()
                .removePrefix("```json")
                .removeSuffix("```")
                .trim()

            val result = json.decodeFromString<GeminiPredictionResult>(cleanedJson)
            Result.success(result)

        } catch (e: Exception) {
            Log.e("GeminiService", "Error: ${e.message}")
            val errorMessage = when {
                e.message?.contains("404") == true -> "Model '$currentModelName' not found. Update Firebase config."
                e is kotlinx.serialization.SerializationException -> "Failed to parse AI response."
                else -> e.localizedMessage ?: "Unknown AI error"
            }
            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun getFantasyTeam(allPlayerNames: List<String>): Result<List<String>> = withContext(Dispatchers.IO) {
        updateConfigFromFirebase()

        if (currentApiKey.isEmpty() || currentApiKey.startsWith("YOUR")) {
            return@withContext Result.failure(Exception("API Key not configured."))
        }

        val model = GenerativeModel(
            modelName = currentModelName,
            apiKey = currentApiKey,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
            }
        )

        val prompt = """
            You are a cricket expert. Given the following list of 22 players (11 from each team), 
            select the best possible Fantasy 11 team (total 11 players).
            Ensure a balanced team (typically 1-2 Wicketkeepers, 3-5 Batsmen, 1-3 All-rounders, 3-5 Bowlers).
            
            Players:
            ${allPlayerNames.joinToString(", ")}
            
            Return a JSON object with:
            {
              "team": ["Player Name 1", "Player Name 2", ..., "Player Name 11"]
            }
        """.trimIndent()

        try {
            val response = model.generateContent(prompt)
            val responseText = response.text ?: return@withContext Result.failure(Exception("Empty response"))

            val cleanedJson = responseText.trim()
                .removePrefix("```json")
                .removeSuffix("```")
                .trim()

            val result = json.decodeFromString<FantasyTeamResult>(cleanedJson)
            Result.success(result.team)

        } catch (e: Exception) {
            Log.e("GeminiService", "Fantasy Team Error: ${e.message}")
            Result.failure(e)
        }
    }

    companion object {
        @Volatile
        private var instance: GeminiPredictionService? = null
        fun getInstance(): GeminiPredictionService {
            return instance ?: synchronized(this) {
                instance ?: GeminiPredictionService().also { instance = it }
            }
        }
    }
}
