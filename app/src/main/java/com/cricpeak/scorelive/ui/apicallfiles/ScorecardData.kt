package com.cricpeak.scorelive.ui.apicallfiles

// Data classes for Scorecard
data class BatsmanStats(
    val name: String,
    val runs: Int,
    val balls: Int,
    val fours: Int,
    val sixes: Int,
    val strikeRate: Double,
    val outStatus: String, // "Not Out", "Bowled by", "Caught by", "LBW by", etc.
    val dismissalDetail: String = "", // e.g., "Bhuvneshwar Kumar"
    val imageUrl: String = "",
    val playerId: Int = 0,
    val battingStyle: String = "N/A",
    val bowlingStyle: String = "N/A",
    val dob: String? = null,
    val birthPlace: String? = null,
    val height: String? = null,
    val bio: String? = null,
    val isCaptain: Boolean = false,
    val isWicketKeeper: Boolean = false
)

data class BowlerStats(
    val name: String,
    val overs: String, // e.g., "4.3"
    val maidens: Int,
    val runs: Int,
    val wickets: Int,
    val economy: Double,
    val imageUrl: String = "",
    val dots: Int = 0,
    val wides: Int = 0,
    val playerId: Int = 0,
    val battingStyle: String = "N/A",
    val bowlingStyle: String = "N/A",
    val dob: String? = null,
    val birthPlace: String? = null,
    val height: String? = null,
    val bio: String? = null,
    val isCaptain: Boolean = false,
    val isWicketKeeper: Boolean = false
)

data class FallOfWicket(
    val playerName: String,
    val runsAtFall: Int,
    val over: String // e.g., "12.4"
)

data class InningsData(
    val teamName: String,
    val teamShort: String,
    val batsmen: List<BatsmanStats>,
    val bowlers: List<BowlerStats>,
    val extras: Int,
    val total: Int,
    val wickets: Int,
    val fallOfWickets: List<FallOfWicket>,
    val inningNumber: Int = 0
)

// Sample Data for Demo
fun getSampleInningsData(): Pair<InningsData, InningsData> {
    val innings1 = InningsData(
        teamName = "India",
        teamShort = "IND",
        batsmen = emptyList(),
        bowlers = emptyList(),
        extras = 0,
        total = 0,
        wickets = 0,
        fallOfWickets = emptyList(),
        inningNumber = 1
    )

    val innings2 = InningsData(
        teamName = "Australia",
        teamShort = "AUS",
        batsmen = emptyList(),
        bowlers = emptyList(),
        extras = 0,
        total = 0,
        wickets = 0,
        fallOfWickets = emptyList(),
        inningNumber = 2
    )

    return Pair(innings1, innings2)
}

/**
 * Extension to map API InningData to UI InningsData
 */
fun InningData.toUIInningsData(teamName: String, teamShort: String, players: List<PlayerDetail>): InningsData {
    val playerMap = players.associateBy { it.id }
    
    return InningsData(
        teamName = teamName,
        teamShort = teamShort,
        batsmen = this.batsmen.map { 
            val player = playerMap[it.playerId]
            BatsmanStats(
                name = player?.displayName ?: it.fullName ?: "Player ${it.playerId}",
                runs = it.runs,
                balls = it.balls,
                fours = it.fours,
                sixes = it.sixes,
                strikeRate = it.strikeRate,
                outStatus = if (it.isOut) "Out" else "Not Out",
                dismissalDetail = it.dismissalText,
                imageUrl = player?.imageUrl ?: "",
                playerId = it.playerId,
                battingStyle = player?.battingStyle ?: player?.battingHand ?: "N/A",
                bowlingStyle = player?.bowlingStyle ?: player?.bowlingType ?: "N/A",
                dob = player?.dob,
                birthPlace = player?.birthPlace,
                height = player?.height,
                bio = player?.bio,
                isCaptain = player?.isCaptain ?: false,
                isWicketKeeper = player?.isWicketKeeper ?: false
            )
        },
        bowlers = this.bowlers.map {
            val player = playerMap[it.playerId]
            BowlerStats(
                name = player?.displayName ?: it.fullName ?: "Bowler ${it.playerId}",
                overs = it.overs,
                maidens = it.maidens,
                runs = it.runs,
                wickets = it.wickets,
                economy = it.economy,
                imageUrl = player?.imageUrl ?: "",
                dots = it.dotBalls,
                wides = it.wides,
                playerId = it.playerId,
                battingStyle = player?.battingStyle ?: player?.battingHand ?: "N/A",
                bowlingStyle = player?.bowlingStyle ?: player?.bowlingType ?: "N/A",
                dob = player?.dob,
                birthPlace = player?.birthPlace,
                height = player?.height,
                bio = player?.bio,
                isCaptain = player?.isCaptain ?: false,
                isWicketKeeper = player?.isWicketKeeper ?: false
            )
        },
        extras = this.totalExtras,
        total = this.runsScored,
        wickets = this.wickets,
        fallOfWickets = this.fallOfWickets.map {
            val player = playerMap[it.playerId]
            FallOfWicket(
                playerName = player?.displayName ?: it.playerName ?: "Player ${it.playerId}",
                runsAtFall = it.runsAtFall,
                over = it.over
            )
        },
        inningNumber = this.inningNumber
    )
}
