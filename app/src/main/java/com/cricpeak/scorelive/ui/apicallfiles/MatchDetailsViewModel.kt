package com.cricpeak.scorelive.ui.apicallfiles

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cricpeak.scorelive.ads.RewardPrefs
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.collections.find

sealed class MatchDetailsState {
    object Loading : MatchDetailsState()
    data class Success(val matchData: MatchDetailsData) : MatchDetailsState()
    data class Error(val message: String) : MatchDetailsState()
}

enum class AnimationType { NONE, FOUR, SIX, WICKET }

data class ScoreAnimation(
    val type: AnimationType,
    val timestamp: Long = System.currentTimeMillis()
)

sealed class PredictionState {
    object Idle : PredictionState()
    object Loading : PredictionState()
    data class Success(val prediction: GeminiPredictionResult) : PredictionState()
    data class Error(val message: String) : PredictionState()
}

sealed class CommentsState {
    object Loading : CommentsState()
    data class Success(val commentsData: CommentsData) : CommentsState()
    data class Error(val message: String) : CommentsState()
}

data class OverSummary(
    val overNumber: Int,
    val balls: List<BallInfo>,
    val totalRuns: Int = 0,
    val totalWickets: Int = 0,
    val isCompleted: Boolean = false
)

sealed class CommentaryEntry {
    data class BallCommentary(
        val over: String,
        val ballResult: String,
        val title: String,
        val description: String,
        val color: Color,
        val length: String? = null,
        val line: String? = null,
        val shotType: String? = null,
        val fieldingPosition: String? = null
    ) : CommentaryEntry()

    data class OverScorecard(
        val overNumber: Int,
        val totalScore: String,
        val totalOvers: String,
        val batsman1: BatsmanStats?,
        val batsman2: BatsmanStats?,
        val bowler: BowlerStats?,
        val runsInOver: Int = 0,
        val wicketsInOver: Int = 0,
        val runRate: Double = 0.0
    ) : CommentaryEntry()
}

data class CommentsData(
    val recentOvers: List<OverSummary> = emptyList(),
    val commentary: List<CommentaryEntry> = emptyList(),
    val totalInnings: Int = 2,
    val currentInning: Int = 1
)

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

    val displayColor: Color
        get() = when {
            isWicket -> Color(0xFFE74C3C)
            isSix -> Color(0xFF8E44AD)
            runs == 4 || (isBoundary && !isSix) -> Color(0xFFF57C00)
            isWide || isNoBall -> Color(0xFF00BFA5)
            isLegBye || isBye -> Color(0xFF16A085)
            else -> Color(0xFF2C3E50)
        }
}

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

    // Reward Ad State
    private val _unlockedUntilOver = MutableStateFlow(2)
    val unlockedUntilOver: StateFlow<Int> = _unlockedUntilOver

    private var currentMatchData: MatchDetailsData? = null
    private var refreshJob: Job? = null

    fun initUnlocks(context: Context, matchId: Int) {
        _unlockedUntilOver.value = RewardPrefs.getOversUnlockedUntil(context, matchId)
    }

    fun unlockMoreOvers(context: Context, matchId: Int, currentOver: Int) {
        val newLimit = currentOver + 2
        _unlockedUntilOver.value = newLimit
        RewardPrefs.setOversUnlockedUntil(context, matchId, newLimit)
    }

    fun loadMatchDetails(matchId: Int, isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _isRefreshing.value = true
            } else {
                _matchDetailsState.value = MatchDetailsState.Loading
            }
            
            try {
                val response = repository.getScorecard(matchId)
                val matchData = response.toMatchDetailsData()
                
                if (isRefresh && currentMatchData != null) {
                    val prev = currentMatchData!!
                    val newInning = matchData.innings.lastOrNull()
                    val oldInning = prev.innings.lastOrNull()
                    
                    val newTotal = newInning?.total ?: 0
                    val oldTotal = oldInning?.total ?: 0
                    val newWickets = newInning?.wickets ?: 0
                    val oldWickets = oldInning?.wickets ?: 0
                    
                    val runsDiff = newTotal - oldTotal
                    
                    when {
                        newWickets > oldWickets -> _scoreAnimationState.value = ScoreAnimation(AnimationType.WICKET)
                        runsDiff == 6 -> _scoreAnimationState.value = ScoreAnimation(AnimationType.SIX)
                        runsDiff == 4 -> _scoreAnimationState.value = ScoreAnimation(AnimationType.FOUR)
                    }
                }

                _matchDetailsState.value = MatchDetailsState.Success(matchData)
                currentMatchData = matchData
                
                if (matchData.isUpcoming && !isRefresh) fetchMatchPrediction(matchData)

                if (!isRefresh && matchData.isLive) {
                    startAutoRefresh(matchId)
                } else if (isRefresh && !matchData.isLive) {
                    refreshJob?.cancel()
                }

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
        val allOversMap = LinkedHashMap<Int, OverDetails>()
        val fullScorecardInning = currentMatchData?.innings?.find { it.inningNumber == inningNumber }
        fullScorecardInning?.recentOvers?.forEach { over -> allOversMap[over.overNumber] = over }
        currentInning?.overs?.forEach { allOversMap[it.overNumber] = it }
        currentInning?.previousOvers?.forEach { over -> allOversMap[over.overNumber] = over }
        currentInning?.currentOver?.let { over -> allOversMap[over.overNumber] = over }
        val overs = allOversMap.values.toList()
        val playersMap = response.players?.associateBy { it.id } ?: emptyMap()
        val totalInningsAvailableInDetails = currentMatchData?.innings?.size ?: 0
        val totalInningsInComments = innings.size
        val totalInningsExpected = if (gameType?.uppercase()?.contains("TEST") == true || totalInningsAvailableInDetails > 2 || totalInningsInComments > 2) 4 else 2
        val sortedOvers = overs.sortedBy { it.overNumber }
        val batsmanSnapshots = mutableMapOf<Int, BatsmanStats>()
        val bowlerSnapshots = mutableMapOf<Int, BowlerStats>()
        val currentPair = mutableSetOf<Int>()
        
        data class OverState(val totalRuns: Int, val totalWickets: Int, val b1: BatsmanStats?, val b2: BatsmanStats?, val bowler: BowlerStats?, val runsInOver: Int, val wicketsInOver: Int, val runRate: Double)
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
                    bowlerSnapshots[ball.bowlerId] = stats.copy(runs = stats.runs + ball.runsConceded, wickets = stats.wickets + if (ball.isWicket) 1 else 0, overs = "${totalBalls / 6}.${totalBalls % 6}")
                }
            }
            val pair = currentPair.toList()
            overStatesMap[over.overNumber] = OverState(
                totalRuns = over.totalInningRuns,
                totalWickets = over.totalInningWickets,
                b1 = pair.getOrNull(0)?.let { batsmanSnapshots[it] },
                b2 = pair.getOrNull(1)?.let { batsmanSnapshots[it] },
                bowler = bowlerSnapshots[lastBowlerId],
                runsInOver = over.runsConceded + over.runsExtras,
                wicketsInOver = over.wickets,
                runRate = over.runRate
            )
        }

        val processedOvers = sortedOvers.map { over ->
            val balls = over.balls?.sortedBy { it.ballNumber } ?: emptyList()
            OverSummary(overNumber = over.overNumber, balls = balls.map { b -> BallInfo(runs = b.teamRuns.takeIf { it > 0 } ?: b.runs, isWicket = b.isWicket, isWide = b.isWide, isNoBall = b.isNoBall, isBoundary = b.isBoundary, isSix = b.runsScored == 6, overNumber = over.overNumber, ballNumber = b.ballNumber, isLegBye = b.isLegByes, isBye = b.isByes) }, totalRuns = over.runsConceded + over.runsExtras, totalWickets = over.wickets, isCompleted = balls.count { !it.isWide && !it.isNoBall } >= 6 || over != sortedOvers.lastOrNull())
        }

        val commentary = mutableListOf<CommentaryEntry>()
        for (over in sortedOvers) {
            val apiBalls = over.balls?.sortedBy { it.ballNumber } ?: continue
            for (ball in apiBalls) {
                val comments = ball.comments ?: continue
                for (comment in comments.filter { it.commentTypeId != "EndOfOver" }) {
                    commentary.add(CommentaryEntry.BallCommentary(ballResult = when { ball.isWicket -> "W"; ball.isWide -> "Wd"; ball.isNoBall -> "Nb"; ball.isLegByes -> "${ball.runs}lb"; ball.isByes -> "${ball.runs}b"; else -> ball.runsScored.toString() }, title = formatCommentaryTitle(comment, ball), over = "${over.overNumber - 1}.${ball.ballNumber}", description = comment.message, color = getBallColor(ball), length = ball.lengthTypeId, line = ball.lineTypeId, shotType = ball.battingShotTypeId, fieldingPosition = ball.fieldingPosition))
                }
            }
            overStatesMap[over.overNumber]?.let { state ->
                commentary.add(CommentaryEntry.OverScorecard(
                    overNumber = over.overNumber,
                    totalScore = "${state.totalRuns}/${state.totalWickets}",
                    totalOvers = "${over.overNumber}",
                    batsman1 = state.b1,
                    batsman2 = state.b2,
                    bowler = state.bowler,
                    runsInOver = state.runsInOver,
                    wicketsInOver = state.wicketsInOver,
                    runRate = state.runRate
                ))
            }
        }
        return CommentsData(processedOvers.takeLast(5), commentary.reversed(), totalInningsExpected, inningNumber)
    }

    private fun getBallColor(ball: BallDetails): Color = when { ball.isWicket -> Color(0xFFE74C3C); ball.runsScored == 6 -> Color(0xFF8E44AD); ball.runsScored == 4 || ball.isBoundary -> Color(0xFFF57C00); ball.isWide || ball.isNoBall -> Color(0xFF00BFA5); ball.isLegByes || ball.isByes -> Color(0xFF16A085); else -> Color(0xFF2C3E50) }

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
            result.onSuccess { _predictionState.value = PredictionState.Success(it) }.onFailure { _predictionState.value = PredictionState.Error(it.message ?: "Unknown error") }
        }
    }
    fun retry(matchId: Int) = loadMatchDetails(matchId)
}
