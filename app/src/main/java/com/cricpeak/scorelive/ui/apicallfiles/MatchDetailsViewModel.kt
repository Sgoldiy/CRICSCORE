package com.cricpeak.scorelive.ui.apicallfiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * States for Match Details loading
 */
sealed class MatchDetailsState {
    object Loading : MatchDetailsState()
    data class Success(val matchData: MatchDetailsData) : MatchDetailsState()
    data class Error(val message: String) : MatchDetailsState()
}

/**
 * Score Animation Types
 */
enum class AnimationType { NONE, FOUR, SIX, WICKET }

data class ScoreAnimation(
    val type: AnimationType,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * State for AI Prediction loading
 */
sealed class PredictionState {
    object Idle : PredictionState()
    object Loading : PredictionState()
    data class Success(val prediction: GeminiPredictionResult) : PredictionState()
    data class Error(val message: String) : PredictionState()
}

/**
 * State for comments / ball-by-ball data
 */
sealed class CommentsState {
    object Loading : CommentsState()
    data class Success(val commentsData: CommentsData) : CommentsState()
    data class Error(val message: String) : CommentsState()
}

/**
 * Represents a completed or current over in the recent balls display
 */
data class OverSummary(
    val overNumber: Int,
    val balls: List<BallInfo>,
    val totalRuns: Int = 0,
    val totalWickets: Int = 0,
    val isCompleted: Boolean = false
)

/**
 * Commentary entry for the Commentary tab
 */
sealed class CommentaryEntry {
    data class BallCommentary(
        val over: String,
        val ballResult: String,
        val title: String,
        val description: String,
        val color: androidx.compose.ui.graphics.Color,
        val length: String? = null,
        val line: String? = null,
        val shotType: String? = null,
        val fieldingPosition: String? = null
    ) : CommentaryEntry()

    data class OverScorecard(
        val overNumber: Int,
        val totalScore: String, // e.g. "124/3"
        val totalOvers: String,
        val batsman1: BatsmanStats?,
        val batsman2: BatsmanStats?,
        val bowler: BowlerStats?,
        val runsInOver: Int = 0,
        val wicketsInOver: Int = 0
    ) : CommentaryEntry()
}

/**
 * Processed comments data for UI consumption
 */
data class CommentsData(
    val recentOvers: List<OverSummary> = emptyList(), // Last 3 completed + current running
    val commentary: List<CommentaryEntry> = emptyList(),
    val totalInnings: Int = 2,
    val currentInning: Int = 1
)

/**
 * Simplified ball info for Recent Balls display
 */
data class BallInfo(
    val runs: Int,
    val isWicket: Boolean,
    val isWide: Boolean,
    val isNoBall: Boolean,
    val isBoundary: Boolean,
    val isSix: Boolean,
    val overNumber: Int = 0,
    val ballNumber: Int = 0,
    val isLegBye: Boolean = false,
    val isBye: Boolean = false
) {
    val displayText: String
        get() = when {
            isWicket -> "W"
            isWide -> "Wd"
            isNoBall -> "Nb"
            isLegBye -> "${runs}Lb"
            isBye -> "${runs}B"
            else -> runs.toString()
        }

    val displayColor: androidx.compose.ui.graphics.Color
        get() = when {
            isWicket -> androidx.compose.ui.graphics.Color(0xFFE74C3C) // Red
            isSix -> androidx.compose.ui.graphics.Color(0xFF8E44AD) // Purple
            runs == 4 || (isBoundary && !isSix) -> androidx.compose.ui.graphics.Color(0xFFF57C00) // Orange for Four
            isWide || isNoBall -> androidx.compose.ui.graphics.Color(0xFF00BFA5) // Teal/Greenish for extras as requested "diff color"
            isLegBye || isBye -> androidx.compose.ui.graphics.Color(0xFF16A085)
            else -> androidx.compose.ui.graphics.Color(0xFF2C3E50)
        }
}

/**
 * ViewModel for Match Details Screen
 */
class MatchDetailsViewModel(
    private val repository: MatchDetailsRepository = MatchDetailsRepository.getInstance(),
    private val geminiService: GeminiPredictionService = GeminiPredictionService.getInstance()
) : ViewModel() {

    private val _matchDetailsState = MutableStateFlow<MatchDetailsState>(MatchDetailsState.Loading)
    val matchDetailsState: StateFlow<MatchDetailsState> = _matchDetailsState

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    val matchDetails: StateFlow<MatchDetailsData?> = _matchDetailsState
        .map { state -> (state as? MatchDetailsState.Success)?.matchData }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _commentsState = MutableStateFlow<CommentsState>(CommentsState.Loading)
    val commentsState: StateFlow<CommentsState> = _commentsState

    private val _predictionState = MutableStateFlow<PredictionState>(PredictionState.Idle)
    val predictionState: StateFlow<PredictionState> = _predictionState

    private val _scoreAnimationState = MutableStateFlow(ScoreAnimation(AnimationType.NONE))
    val scoreAnimationState: StateFlow<ScoreAnimation> = _scoreAnimationState

    private var currentMatchData: MatchDetailsData? = null
    private var refreshJob: kotlinx.coroutines.Job? = null

    fun loadMatchDetails(matchId: Int, isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _isRefreshing.value = true
            } else {
                _matchDetailsState.value = MatchDetailsState.Loading
            }
            
            try {
                // First try getting scorecard as it's more reliable for match ID
                val response = repository.getScorecard(matchId)
                val matchData = response.toMatchDetailsData()
                
                // --- SCORE ANIMATION DETECTION ---
                if (isRefresh && currentMatchData != null) {
                    val prev = currentMatchData!!
                    val newTotal = matchData.innings.lastOrNull()?.runsScored ?: 0
                    val oldTotal = prev.innings.lastOrNull()?.runsScored ?: 0
                    val newWickets = matchData.innings.lastOrNull()?.wickets ?: 0
                    val oldWickets = prev.innings.lastOrNull()?.wickets ?: 0
                    
                    val runsDiff = newTotal - oldTotal
                    
                    when {
                        newWickets > oldWickets -> {
                            _scoreAnimationState.value = ScoreAnimation(AnimationType.WICKET)
                        }
                        runsDiff == 6 -> {
                            _scoreAnimationState.value = ScoreAnimation(AnimationType.SIX)
                        }
                        runsDiff == 4 -> {
                            _scoreAnimationState.value = ScoreAnimation(AnimationType.FOUR)
                        }
                    }
                }

                _matchDetailsState.value = MatchDetailsState.Success(matchData)
                currentMatchData = matchData
                
                if (matchData.isUpcoming && !isRefresh) fetchMatchPrediction(matchData)

                // --- AUTO-REFRESH LOGIC ---
                if (!isRefresh && matchData.isLive) {
                    startAutoRefresh(matchId)
                } else if (isRefresh && !matchData.isLive) {
                    refreshJob?.cancel()
                }

                // Load comments for the latest inning
                val currentInningNum = (_commentsState.value as? CommentsState.Success)?.commentsData?.currentInning
                val inningToLoad = currentInningNum ?: if (matchData.innings.isNotEmpty()) {
                    matchData.innings.maxByOrNull { it.inningNumber }?.inningNumber ?: 1
                } else 1
                
                loadComments(matchId, inningToLoad, isRefresh)
            } catch (e: Exception) {
                if (!isRefresh) _matchDetailsState.value = MatchDetailsState.Error(e.localizedMessage ?: "Error")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private fun startAutoRefresh(matchId: Int) {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (isActive) {
                delay(10000)
                loadMatchDetails(matchId, isRefresh = true)
            }
        }
    }

    fun loadComments(fixtureId: Int, inningNumber: Int, isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (!isRefresh) _commentsState.value = CommentsState.Loading
            try {
                // Fetch with a high oversCount (500) to ensure we get full data for T20 (20), ODI (50), Test (500+)
                val response = repository.getComments(fixtureId, inningNumber, 500)
                val commentsData = mapCommentsResponse(response, currentMatchData?.gameType, inningNumber)
                _commentsState.value = CommentsState.Success(commentsData)
            } catch (e: Exception) {
                if (!isRefresh) _commentsState.value = CommentsState.Error(e.localizedMessage ?: "Error")
            }
        }
    }

    fun clearAnimation() {
        _scoreAnimationState.value = ScoreAnimation(AnimationType.NONE)
    }

    private fun mapCommentsResponse(response: CommentsResponse, gameType: String? = null, inningNumber: Int = 1): CommentsData {
        val innings = response.innings ?: emptyList()
        val currentInning = innings.find { it.inningNumber == inningNumber } ?: innings.firstOrNull()
        
        // Merge all over sources: "Overs" (scorecard style), "PreviousOvers" + "CurrentOver" (comments style)
        // Use a LinkedHashMap to deduplicate by overNumber while preserving order
        val allOversMap = LinkedHashMap<Int, OverDetails>()
        
        // --- FIX FOR 10-OVER LIMIT ---
        // The API's 'comments' endpoint only returns the last 10 overs.
        // However, the scorecard API (currentMatchData) has ALL the overs. We merge them here!
        val fullScorecardInning = currentMatchData?.innings?.find { it.inningNumber == inningNumber }
        fullScorecardInning?.recentOvers?.forEach { over ->
            allOversMap[over.overNumber] = over
        }

        // First add "Overs" (general source)
        currentInning?.overs?.forEach { allOversMap[it.overNumber] = it }
        // Then add "PreviousOvers" (completed overs from comments API) - overwriting any stale Scorecard ones
        currentInning?.previousOvers?.forEach { over ->
            allOversMap[over.overNumber] = over
        }
        // Finally add "CurrentOver" (in-progress over from comments API)
        currentInning?.currentOver?.let { over ->
            allOversMap[over.overNumber] = over
        }
        val overs = allOversMap.values.toList()
        
        android.util.Log.d("MatchDetailsVM", "Commentary: Found ${overs.size} overs total " +
            "(Overs=${currentInning?.overs?.size ?: 0}, " +
            "PreviousOvers=${currentInning?.previousOvers?.size ?: 0}, " +
            "CurrentOver=${if (currentInning?.currentOver != null) 1 else 0})")
        
        val playersMap = response.players?.associateBy { it.id } ?: emptyMap()
        
        val totalInningsExpected = when (gameType?.uppercase()) {
            "TEST", "FIRSTCLASS", "FIRST CLASS", "TESTS" -> 4
            else -> 2
        }

        // Sort overs to ensure correct order
        val sortedOvers = overs.sortedBy { it.overNumber }
        
        // --- Build per-over player snapshots for the commentary scorecard ---
        // We use runsScored (batter's own runs) and runsConceded (charged to bowler)
        // instead of the raw "runs" field which includes extras like leg byes.
        val batsmanSnapshots = mutableMapOf<Int, BatsmanStats>()
        val bowlerSnapshots = mutableMapOf<Int, BowlerStats>()
        val currentPair = mutableSetOf<Int>()
        
        data class OverState(
            val totalRuns: Int, val totalWickets: Int, 
            val b1: BatsmanStats?, val b2: BatsmanStats?, val bowler: BowlerStats?,
            val runsInOver: Int, val wicketsInOver: Int
        )
        val overStatesMap = mutableMapOf<Int, OverState>()

        for (over in sortedOvers) {
            val apiBalls = over.balls?.sortedBy { it.ballNumber } ?: continue
            var lastBowlerId = 0

            for (ball in apiBalls) {
                lastBowlerId = ball.bowlerId
                
                if (ball.isWicket) currentPair.remove(ball.batterId)

                if (ball.batterId != 0) {
                    if (!currentPair.contains(ball.batterId) && currentPair.size < 2) currentPair.add(ball.batterId)
                    val stats = batsmanSnapshots[ball.batterId] ?: run {
                        val p = playersMap[ball.batterId]
                        BatsmanStats(name = p?.displayName ?: "Player", runs = 0, balls = 0, fours = 0, sixes = 0, strikeRate = 0.0, outStatus = "Not Out", imageUrl = p?.imageUrl ?: "", playerId = ball.batterId)
                    }
                    // Use runsScored (batter's actual runs, excludes leg byes/byes)
                    batsmanSnapshots[ball.batterId] = stats.copy(
                        runs = stats.runs + ball.runsScored,
                        balls = stats.balls + if (!ball.isWide) 1 else 0,
                        fours = stats.fours + if (ball.isBoundary && ball.runsScored == 4) 1 else 0,
                        sixes = stats.sixes + if (ball.runsScored == 6) 1 else 0,
                        outStatus = if (ball.isWicket) "Out" else "Not Out"
                    )
                }

                if (ball.bowlerId != 0) {
                    val stats = bowlerSnapshots[ball.bowlerId] ?: run {
                        val p = playersMap[ball.bowlerId]
                        BowlerStats(name = p?.displayName ?: "Bowler", overs = "0.0", maidens = 0, runs = 0, wickets = 0, economy = 0.0, imageUrl = p?.imageUrl ?: "", playerId = ball.bowlerId)
                    }
                    val totalBalls = (stats.overs.toDoubleOrNull() ?: 0.0).let { (it.toInt() * 6) + ((it * 10) % 10).toInt() } + if (!ball.isWide && !ball.isNoBall) 1 else 0
                    // Use runsConceded (charged to bowler, excludes byes/leg byes)
                    bowlerSnapshots[ball.bowlerId] = stats.copy(
                        runs = stats.runs + ball.runsConceded,
                        wickets = stats.wickets + if (ball.isWicket) 1 else 0,
                        overs = "${totalBalls / 6}.${totalBalls % 6}"
                    )
                }
            }

            val pair = currentPair.toList()
            // Use TotalInningRuns/TotalInningWickets from the API — already calculated correctly!
            overStatesMap[over.overNumber] = OverState(
                totalRuns = over.totalInningRuns,
                totalWickets = over.totalInningWickets,
                b1 = pair.getOrNull(0)?.let { batsmanSnapshots[it] },
                b2 = pair.getOrNull(1)?.let { batsmanSnapshots[it] },
                bowler = bowlerSnapshots[lastBowlerId],
                runsInOver = over.runsConceded + over.runsExtras, // actual total runs in over
                wicketsInOver = over.wickets
            )
        }

        // Horizontal Recent Balls strip (show last few overs)
        val processedOvers = sortedOvers.map { over ->
            val balls = over.balls?.sortedBy { it.ballNumber } ?: emptyList()
            OverSummary(
                overNumber = over.overNumber,
                balls = balls.map { b ->
                    BallInfo(
                        // Use teamRuns for the ball strip display (what the team actually got)
                        runs = b.teamRuns.takeIf { it > 0 } ?: b.runs,
                        isWicket = b.isWicket,
                        isWide = b.isWide,
                        isNoBall = b.isNoBall,
                        isBoundary = b.isBoundary,
                        isSix = b.runsScored == 6,
                        overNumber = over.overNumber,
                        ballNumber = b.ballNumber,
                        isLegBye = b.isLegByes,
                        isBye = b.isByes
                    )
                },
                totalRuns = over.runsConceded + over.runsExtras,
                totalWickets = over.wickets,
                isCompleted = balls.count { !it.isWide && !it.isNoBall } >= 6 || over != sortedOvers.lastOrNull()
            )
        }

        // Commentary: Latest first
        val commentary = mutableListOf<CommentaryEntry>()
        for (over in sortedOvers) {
            val apiBalls = over.balls?.sortedBy { it.ballNumber } ?: continue
            for (ball in apiBalls) {
                val comments = ball.comments ?: continue
                for (comment in comments.filter { it.commentTypeId != "EndOfOver" }) {
                    commentary.add(
                        CommentaryEntry.BallCommentary(
                            // Ball result label: W for wicket, otherwise show team runs on ball
                            ballResult = when {
                                ball.isWicket -> "W"
                                ball.isWide -> "Wd"
                                ball.isNoBall -> "Nb"
                                ball.isLegByes -> "${ball.runs}lb"
                                ball.isByes -> "${ball.runs}b"
                                else -> ball.runsScored.toString()
                            },
                            title = formatCommentaryTitle(comment, ball),
                            over = "${over.overNumber - 1}.${ball.ballNumber}",
                            description = comment.message,
                            color = getBallColor(ball),
                            length = ball.lengthTypeId,
                            line = ball.lineTypeId,
                            shotType = ball.battingShotTypeId,
                            fieldingPosition = ball.fieldingPosition
                        )
                    )
                }
            }

            // OverScorecard Title - ADDED AFTER BALLS for chronological order
            // Since we reverse the final list, adding it AFTER balls means it shows ABOVE them in the UI (after the over completes)
            overStatesMap[over.overNumber]?.let { state ->
                commentary.add(
                    CommentaryEntry.OverScorecard(
                        overNumber = over.overNumber,
                        totalScore = "${state.totalRuns}/${state.totalWickets}",
                        totalOvers = "${over.overNumber}",
                        batsman1 = state.b1, batsman2 = state.b2, bowler = state.bowler,
                        runsInOver = state.runsInOver, wicketsInOver = state.wicketsInOver
                    )
                )
            }
        }

        return CommentsData(processedOvers.takeLast(5), commentary.reversed(), totalInningsExpected, inningNumber)
    }

    private fun getBallColor(ball: BallDetails): androidx.compose.ui.graphics.Color {
        return when {
            ball.isWicket -> androidx.compose.ui.graphics.Color(0xFFE74C3C) // Red
            ball.runsScored == 6 -> androidx.compose.ui.graphics.Color(0xFF8E44AD) // Purple
            ball.runsScored == 4 || ball.isBoundary -> androidx.compose.ui.graphics.Color(0xFFF57C00) // Orange
            ball.isWide || ball.isNoBall -> androidx.compose.ui.graphics.Color(0xFF00BFA5) // Teal
            ball.isLegByes || ball.isByes -> androidx.compose.ui.graphics.Color(0xFF16A085)
            else -> androidx.compose.ui.graphics.Color(0xFF2C3E50)
        }
    }

    private fun formatCommentaryTitle(comment: CommentDetails, ball: BallDetails): String {
        val m = comment.message
        return when {
            m.startsWith("OUT!") || m.startsWith("SIX!") || m.startsWith("FOUR!") -> m.substringBefore(".").trim()
            else -> comment.commentTypeId
        }
    }


    private fun fetchMatchPrediction(matchData: MatchDetailsData) {
        viewModelScope.launch {
            _predictionState.value = PredictionState.Loading
            val result = geminiService.getMatchPrediction(matchData)
            result.onSuccess {
                _predictionState.value = PredictionState.Success(it)
            }.onFailure {
                _predictionState.value = PredictionState.Error(it.message ?: "Unknown error")
            }
        }
    }
    fun retry(matchId: Int) = loadMatchDetails(matchId)
}
