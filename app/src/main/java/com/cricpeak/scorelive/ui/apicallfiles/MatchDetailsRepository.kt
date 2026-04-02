package com.cricpeak.scorelive.ui.apicallfiles

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.ConcurrentHashMap

/**
 * Repository layer for Match Details
 * Handles all data operations and caching if needed
 */
class MatchDetailsRepository(private val apiService: CricketApiService) {

    private val scorecardCache = ConcurrentHashMap<Int, ScorecardResponse>()
    private val commentsCache = ConcurrentHashMap<String, CommentsResponse>()

    fun getCachedScorecard(matchId: Int): ScorecardResponse? = scorecardCache[matchId]

    /**
     * Silently fetches data in the background and stores it in memory.
     * Prevents lag when a user clicks a match.
     */
    suspend fun preloadMatchData(matchId: Int) {
        try {
            // Basic debouncing (skip heavy auto-pagination for preloads)
            val scorecard = apiService.getScorecard(matchId)
            scorecardCache[matchId] = scorecard
            
            // Only preload the first page of comments to be efficient
            val targetInning = scorecard.fixture?.innings?.maxByOrNull { it.inningNumber }?.inningNumber ?: 1
            val cacheKey = "${matchId}_${targetInning}"
            
            if (!commentsCache.containsKey(cacheKey)) {
                val comments = apiService.getComments(fixtureId = matchId, inningNumber = targetInning)
                commentsCache[cacheKey] = comments
            }
        } catch (e: Exception) {
            android.util.Log.e("MatchDetailsRepo", "Error preloading $matchId: ${e.message}")
        }
    }

    /**
     * Fetch match details by match ID
     * @param matchId The ID of the match (FixtureId)
     * @return MatchDetailsResponse with all match data
     */
    suspend fun getMatchDetails(matchId: Int): MatchDetailsResponse {
        // Use getScorecard instead of getMatchDetails as the latter might not be supported or requires different ID
        return apiService.getScorecard(matchId)
    }

    /**
     * Fetches ALL overs for an inning by auto-paginating using LastOverNumber.
     * The API only returns ~10 overs per request in the "Overs" field.
     * This method progressively emits pages using Flow to prevent UI lag.
     */
    fun getComments(fixtureId: Int, inningNumber: Int = 1, count: Int = 500): Flow<CommentsResponse> = flow {
        val cacheKey = "${fixtureId}_${inningNumber}"
        try {
            // Emits preloaded data immediately if available! Zero latency for the user UI!
            val cachedComments = commentsCache[cacheKey]
            if (cachedComments != null) {
                emit(cachedComments)
            }

            // First call - get the most recent overs (matches Java's getMatchComments)
            val firstResponse = apiService.getComments(
                fixtureId = fixtureId,
                inningNumber = inningNumber
            )

            // Update cache so subsequent opens are lightning fast with the very latest data
            commentsCache[cacheKey] = firstResponse
            
            // Only emit if it's new/first or we didn't have cache to avoid double-bouncing identical UI states
            if (cachedComments == null) {
                emit(firstResponse)
            }
        } catch (e: Exception) {
            android.util.Log.e("MatchDetailsRepo", "Error fetching comments: ${e.message}")
            if (commentsCache[cacheKey] == null) {
                emit(CommentsResponse())
            }
        }
    }

    /**
     * Manually fetch an older page of commentary. Used for scroll-based pagination.
     */
    suspend fun getPaginatedComments(fixtureId: Int, inningNumber: Int, lastOverNumber: Int): CommentsResponse {
        return try {
            apiService.getComments(
                fixtureId = fixtureId,
                inningNumber = inningNumber,
                lastOverNumber = lastOverNumber
            )
        } catch (e: Exception) {
            android.util.Log.e("MatchDetailsRepo", "Pagination error at over $lastOverNumber: ${e.message}")
            CommentsResponse()
        }
    }

    /**
     * Fetch scorecard details by fixture ID and optionally inning number
     * @param fixtureId The ID of the fixture
     * @param inningNumber The inning number (1 or 2)
     * @return ScorecardResponse with detailed player stats
     */
    suspend fun getScorecard(fixtureId: Int, inningNumber: Int? = null): ScorecardResponse {
        return try {
            val liveData = apiService.getScorecard(fixtureId, inningNumber)
            scorecardCache[fixtureId] = liveData
            liveData
        } catch (e: Exception) {
            // Return cached response if it fails (like offline routing) or empty response to not break UI
            scorecardCache[fixtureId] ?: ScorecardResponse(fixture = MatchDetails(id = fixtureId, innings = emptyList()))
        }
    }

    companion object {
        private var instance: MatchDetailsRepository? = null

        fun getInstance(): MatchDetailsRepository {
            return instance ?: MatchDetailsRepository(CricketApiService.create()).also {
                instance = it
            }
        }
    }
}
