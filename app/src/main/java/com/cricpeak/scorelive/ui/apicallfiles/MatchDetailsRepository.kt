package com.cricpeak.scorelive.ui.apicallfiles

/**
 * Repository layer for Match Details
 * Handles all data operations and caching if needed
 */
class MatchDetailsRepository(private val apiService: CricketApiService) {

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
     * Fetches all overs for an inning. 
     * By passing a high count (e.g., 500), the API will return all overs.
     */
    suspend fun getComments(fixtureId: Int, inningNumber: Int = 1, count: Int = 500): CommentsResponse {
        return try {
            apiService.getComments(
                fixtureId = fixtureId,
                inningNumber = inningNumber,
                completedOversCount = count,
                includePastOvers = true,
                includePreviousOvers = true
            )
        } catch (e: Exception) {
            android.util.Log.e("MatchDetailsRepo", "Error fetching comments: ${e.message}")
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
            apiService.getScorecard(fixtureId, inningNumber)
        } catch (e: Exception) {
            // Return empty response if it fails to not break the whole UI
            ScorecardResponse(fixture = MatchDetails(id = fixtureId, innings = emptyList()))
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
