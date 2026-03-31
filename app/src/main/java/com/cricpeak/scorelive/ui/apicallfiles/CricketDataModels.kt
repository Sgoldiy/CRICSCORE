package com.cricpeak.scorelive.ui.apicallfiles

import com.google.gson.annotations.SerializedName

data class FixturesResponse(
    @SerializedName("UpcomingFixtures") val upcomingFixtures: List<FixtureContainer> = emptyList(),
    @SerializedName("InProgressFixtures") val inProgressFixtures: List<FixtureContainer> = emptyList(),
    @SerializedName("CompletedFixtures") val completedFixtures: List<FixtureContainer> = emptyList()
)

data class FixtureContainer(
    @SerializedName("Competition") val competition: Competition,
    @SerializedName("HomeTeam") val homeTeam: Team,
    @SerializedName("AwayTeam") val awayTeam: Team,
    @SerializedName("Venue") val venue: Venue,
    @SerializedName("Innings") val innings: List<Inning>? = null,
    @SerializedName("Id") val id: Int,
    @SerializedName("Name") val name: String,
    @SerializedName("StartDateTime") val startDateTime: String,
    @SerializedName("ResultText") val resultText: String? = null,
    @SerializedName("IsLive") val isLive: Boolean,
    @SerializedName("IsCompleted") val isCompleted: Boolean,
    @SerializedName("IsInProgress") val isInProgress: Boolean,
    @SerializedName("GameType") val gameType: String
)

data class Competition(
    @SerializedName("Id") val id: Int,
    @SerializedName("Name") val name: String,
    @SerializedName("ImageUrl") val imageUrl: String
)

data class Team(
    @SerializedName("Id") val id: Int,
    @SerializedName("Name") val name: String,
    @SerializedName("ShortName") val shortName: String,
    @SerializedName("LogoUrl") val logoUrl: String
)

data class Venue(
    @SerializedName("Id") val id: Int,
    @SerializedName("Name") val name: String,
    @SerializedName("City") val city: String
)

data class Inning(
    @SerializedName("InningNumber") val inningNumber: Int,
    @SerializedName("RunsScored") val runsScored: Int,
    @SerializedName("OversBowled") val oversBowled: String,
    @SerializedName("NumberOfWicketsFallen") val wickets: Int,
    @SerializedName("BattingTeamId") val battingTeamId: Int = 0
)

// ==================== MATCH DETAILS & SCORECARD API MODELS ====================

data class MatchDetailsResponse(
    @SerializedName("Fixture") val fixture: MatchDetails? = null,
    @SerializedName("Players") val players: List<PlayerDetail>? = null
)

typealias ScorecardResponse = MatchDetailsResponse

data class MatchDetails(
    @SerializedName("Competition") val competition: CompetitionDetails? = null,
    @SerializedName("HomeTeam") val homeTeam: TeamDetails? = null,
    @SerializedName("AwayTeam") val awayTeam: TeamDetails? = null,
    @SerializedName("Venue") val venue: VenueDetails? = null,
    @SerializedName("Innings") val innings: List<InningDetails>? = null,
    @SerializedName("Players") val players: List<PlayerDetail>? = null,
    @SerializedName("Id") val id: Int = 0,
    @SerializedName("Name") val name: String = "",
    @SerializedName("StartDateTime") val startDateTime: String = "",
    @SerializedName("EndDateTime") val endDateTime: String = "",
    @SerializedName("GameType") val gameType: String = "",
    @SerializedName("IsLive") val isLive: Boolean = false,
    @SerializedName("IsCompleted") val isCompleted: Boolean = false,
    @SerializedName("IsInProgress") val isInProgress: Boolean = false,
    @SerializedName("ResultText") val resultText: String? = null,
    @SerializedName("TossResult") val tossResult: String? = null,
    @SerializedName("TossDecision") val tossDecision: String? = null,
    @SerializedName("TotalOvers") val totalOvers: String = "20",
    @SerializedName("OversRemaining") val oversRemaining: String? = null,
    @SerializedName("GameStatus") val gameStatus: String? = null,
    @SerializedName("Officials") val officials: List<Official>? = null,
    @SerializedName("TicketUrl") val ticketUrl: String? = null,
    @SerializedName("WinningMargin") val winningMargin: Int? = null,
    @SerializedName("WinType") val winType: String? = null,
    @SerializedName("Tour") val tour: TourDetails? = null
)

data class PlayerDetail(
    @SerializedName("Id") val id: Int,
    @SerializedName("DisplayName") val displayName: String,
    @SerializedName("FirstName") val firstName: String? = null,
    @SerializedName("LastName") val lastName: String? = null,
    @SerializedName("MiddleName") val middleName: String? = null,
    @SerializedName("Initials") val initials: String? = null,
    @SerializedName("ImageUrl") val imageUrl: String? = null,
    @SerializedName("TeamId") val teamId: Int = 0,
    @SerializedName("Type") val type: String? = null,
    @SerializedName("BattingHand") val battingHand: String? = null,
    @SerializedName("BowlingHand") val bowlingHand: String? = null,
    @SerializedName("BowlingType") val bowlingType: String? = null,
    @SerializedName("DOB") val dob: String? = null,
    @SerializedName("BirthPlace") val birthPlace: String? = null,
    @SerializedName("Height") val height: String? = null,
    @SerializedName("Bio") val bio: String? = null,
    @SerializedName("IsCaptain") val isCaptain: Boolean = false,
    @SerializedName("IsWicketKeeper") val isWicketKeeper: Boolean = false,
    @SerializedName("BattingStyle") val battingStyle: String? = null,
    @SerializedName("BowlingStyle") val bowlingStyle: String? = null
)

data class CompetitionDetails(
    @SerializedName("Id") val id: Int = 0,
    @SerializedName("Name") val name: String = "",
    @SerializedName("ImageUrl") val imageUrl: String = "",
    @SerializedName("BannerImageUrl") val bannerImageUrl: String = "",
    @SerializedName("Formats") val formats: List<Format>? = null
)

data class Format(
    @SerializedName("DisplayName") val displayName: String = "",
    @SerializedName("AssociatedMatchType") val associatedMatchType: String = "",
    @SerializedName("SeriesFormatName") val seriesFormatName: String = ""
)

data class TeamDetails(
    @SerializedName("Id") val id: Int = 0,
    @SerializedName("Name") val name: String = "",
    @SerializedName("ShortName") val shortName: String = "",
    @SerializedName("TeamColor") val teamColor: String = "#000000",
    @SerializedName("LogoUrl") val logoUrl: String = "",
    @SerializedName("BackgroundImageUrl") val backgroundImageUrl: String = "",
    @SerializedName("IsHomeTeam") val isHomeTeam: Boolean = false,
    @SerializedName("IsTossWinner") val isTossWinner: Boolean = false,
    @SerializedName("IsMatchWinner") val isMatchWinner: Boolean = false
)

data class VenueDetails(
    @SerializedName("Id") val id: Int = 0,
    @SerializedName("Name") val name: String = "",
    @SerializedName("City") val city: String = "",
    @SerializedName("Latitude") val latitude: String = "",
    @SerializedName("Longitude") val longitude: String = ""
)

data class InningDetails(
    @SerializedName("Id") val id: Int = 0,
    @SerializedName("FixtureId") val fixtureId: Int = 0,
    @SerializedName("InningNumber") val inningNumber: Int = 0,
    @SerializedName("BattingTeamId") val battingTeamId: Int = 0,
    @SerializedName("BowlingTeamId") val bowlingTeamId: Int = 0,
    @SerializedName("RunsScored") val runsScored: Int = 0,
    @SerializedName("NumberOfWicketsFallen") val wickets: Int = 0,
    @SerializedName("OversBowled") val oversBowled: String = "0.0",
    @SerializedName("CurrentRunRate") val currentRunRate: Double = 0.0,
    @SerializedName("RequiredRunRate") val requiredRunRate: Double? = null,
    @SerializedName("TotalExtras") val totalExtras: Int = 0,
    @SerializedName("ByesRuns") val byes: Int = 0,
    @SerializedName("LegByesRuns") val legByes: Int = 0,
    @SerializedName("NoBalls") val noBalls: Int = 0,
    @SerializedName("WideBalls") val wides: Int = 0,
    @SerializedName("Batsmen") val batsmen: List<BatsmanDetails>? = null,
    @SerializedName("Bowlers") val bowlers: List<BowlerDetails>? = null,
    @SerializedName("Wickets") val fallOfWickets: List<WicketDetails>? = null,
    @SerializedName("Partnership") val partnership: PartnershipDetails? = null,
    // "Overs" is used by the scorecard endpoint
    @SerializedName("Overs") val overs: List<OverDetails>? = null,
    // The comments endpoint splits overs into PreviousOvers (completed) + CurrentOver (in-progress)
    @SerializedName("PreviousOvers") val previousOvers: List<OverDetails>? = null,
    @SerializedName("CurrentOver") val currentOver: OverDetails? = null
)

data class OverDetails(
    @SerializedName("Id") val id: Int = 0,
    @SerializedName("OverNumber") val overNumber: Int = 0,
    @SerializedName("RunsConceded") val runsConceded: Int = 0,
    @SerializedName("Wickets") val wickets: Int = 0,
    // Running totals at the end of this over — use these directly instead of calculating
    @SerializedName("TotalInningRuns") val totalInningRuns: Int = 0,
    @SerializedName("TotalInningWickets") val totalInningWickets: Int = 0,
    @SerializedName("TotalRuns") val totalRuns: Int = 0,
    @SerializedName("Runrate") val runRate: Double = 0.0,
    @SerializedName("RunsExtras") val runsExtras: Int = 0,
    @SerializedName("Balls") val balls: List<BallDetails>? = null
)

data class BallDetails(
    @SerializedName("BallNumber") val ballNumber: Int = 0,
    @SerializedName("IsWicket") val isWicket: Boolean = false,
    // "Runs" = total runs on the delivery including extras (leg byes, byes etc.)
    @SerializedName("Runs") val runs: Int = 0,
    // "RunsScored" = batter's own runs only (excludes extras like leg byes)
    @SerializedName("RunsScored") val runsScored: Int = 0,
    // "RunsConceded" = runs charged to the bowler's account (excludes byes/leg byes)
    @SerializedName("RunsConceded") val runsConceded: Int = 0,
    // "TeamRuns" = team runs credited on this ball
    @SerializedName("TeamRuns") val teamRuns: Int = 0,
    @SerializedName("IsWide") val isWide: Boolean = false,
    @SerializedName("IsNoBall") val isNoBall: Boolean = false,
    @SerializedName("IsBoundry") val isBoundary: Boolean = false,
    @SerializedName("IsLegByes") val isLegByes: Boolean = false,
    @SerializedName("IsByes") val isByes: Boolean = false,
    @SerializedName("Comments") val comments: List<CommentDetails>? = null,
    @SerializedName("BowlerPlayerId") val bowlerId: Int = 0,
    @SerializedName("BattingPlayerId") val batterId: Int = 0,
    @SerializedName("LengthTypeId") val lengthTypeId: String? = null,
    @SerializedName("LineTypeId") val lineTypeId: String? = null,
    @SerializedName("BattingShotTypeId") val battingShotTypeId: String? = null,
    @SerializedName("FieldingPosition") val fieldingPosition: String? = null
)

data class CommentDetails(
    @SerializedName("Message") val message: String = "",
    @SerializedName("CommentTypeId") val commentTypeId: String = ""
)

data class BatsmanDetails(
    @SerializedName("PlayerId") val playerId: Int = 0,
    @SerializedName("FullName") val fullName: String? = null,
    @SerializedName("RunsScored") val runsScored: Int = 0,
    @SerializedName("BallsFaced") val balls: Int = 0,
    @SerializedName("FoursScored") val fours: Int = 0,
    @SerializedName("SixesScored") val sixes: Int = 0,
    @SerializedName("StrikeRate") val strikeRate: Double = 0.0,
    @SerializedName("DismissalText") val dismissalText: String = "",
    @SerializedName("IsOut") val isOut: Boolean = false,
    @SerializedName("IsOnStrike") val isOnStrike: Boolean = false,
    @SerializedName("BattingOrder") val battingOrder: Int = 0,
    @SerializedName("IsBatting") val isBatting: Boolean = false
)

data class BowlerDetails(
    @SerializedName("PlayerId") val playerId: Int = 0,
    @SerializedName("FullName") val fullName: String? = null,
    @SerializedName("OversBowled") val overs: String = "0.0",
    @SerializedName("MaidensBowled") val maidens: Int = 0,
    @SerializedName("RunsConceded") val runs: Int = 0,
    @SerializedName("WicketsTaken") val wickets: Int = 0,
    @SerializedName("Economy") val economy: Double = 0.0,
    @SerializedName("DotBalls") val dotBalls: Int = 0,
    @SerializedName("WideBalls") val wides: Int = 0,
    @SerializedName("NoBalls") val noBalls: Int = 0
)

data class WicketDetails(
    @SerializedName("PlayerId") val playerId: Int = 0,
    @SerializedName("PlayerName") val playerName: String? = null,
    @SerializedName("Runs") val runsAtFall: Int = 0,
    @SerializedName("Order") val order: Int = 0,
    @SerializedName("OverBallDisplay") val over: String = "0.0"
)

data class PartnershipDetails(
    @SerializedName("TotalRunsScored") val totalRuns: Int = 0,
    @SerializedName("TotalBallsFaced") val totalBalls: Int = 0,
    @SerializedName("Runrate") val runRate: Double = 0.0,
    @SerializedName("IsCurrent") val isCurrent: Boolean = false
)

data class TourDetails(
    @SerializedName("Id") val id: Int = 0,
    @SerializedName("Name") val name: String = ""
)

data class Official(
    @SerializedName("FirstName") val firstName: String = "",
    @SerializedName("LastName") val lastName: String = "",
    @SerializedName("Initials") val initials: String = "",
    @SerializedName("UmpireType") val role: String = "",
    @SerializedName("HasRetired") val hasRetired: Boolean = false
)

// ==================== COMMENTS API MODEL ====================
// The comments endpoint returns Innings, HomeTeam, AwayTeam, Players at root level
// (NOT inside a Fixture wrapper like the scorecard endpoint)

data class CommentsResponse(
    @SerializedName("Innings") val innings: List<InningDetails>? = null,
    @SerializedName("HomeTeam") val homeTeam: TeamDetails? = null,
    @SerializedName("AwayTeam") val awayTeam: TeamDetails? = null,
    @SerializedName("Players") val players: List<PlayerDetail>? = null
)

// ==================== UI DATA MODELS ====================

data class MatchDetailsData(
    val id: Int,
    val matchName: String,
    val competition: CompetitionData,
    val homeTeam: TeamData,
    val awayTeam: TeamData,
    val venue: VenueData,
    val startDateTime: String,
    val gameType: String,
    val isLive: Boolean,
    val isCompleted: Boolean,
    val isInProgress: Boolean,
    val isUpcoming: Boolean,
    val tossResult: String,
    val resultText: String?,
    val innings: List<InningsData>,
    val players: List<PlayerDetail>
)

data class TeamData(
    val id: Int,
    val name: String,
    val shortName: String,
    val logoUrl: String,
    val isMatchWinner: Boolean = false
)

data class VenueData(
    val name: String,
    val city: String
)

data class CompetitionData(
    val name: String,
    val imageUrl: String
)



/**
 * Extension to map MatchDetailsResponse to MatchDetailsData
 */
fun MatchDetailsResponse.toMatchDetailsData(): MatchDetailsData {
    val f = this.fixture ?: MatchDetails()
    val players = this.players ?: f.players ?: emptyList()
    
    return MatchDetailsData(
        id = f.id,
        matchName = f.name,
        competition = CompetitionData(
            name = f.competition?.name ?: "",
            imageUrl = f.competition?.imageUrl ?: ""
        ),
        homeTeam = TeamData(
            id = f.homeTeam?.id ?: 0,
            name = f.homeTeam?.name ?: "Home",
            shortName = f.homeTeam?.shortName ?: "HOME",
            logoUrl = f.homeTeam?.logoUrl ?: "",
            isMatchWinner = f.homeTeam?.isMatchWinner ?: false
        ),
        awayTeam = TeamData(
            id = f.awayTeam?.id ?: 0,
            name = f.awayTeam?.name ?: "Away",
            shortName = f.awayTeam?.shortName ?: "AWAY",
            logoUrl = f.awayTeam?.logoUrl ?: "",
            isMatchWinner = f.awayTeam?.isMatchWinner ?: false
        ),
        venue = VenueData(
            name = f.venue?.name ?: "Venue",
            city = f.venue?.city ?: ""
        ),
        startDateTime = f.startDateTime.convertGMTToLocal(),
        gameType = f.gameType,
        isLive = f.isLive,
        isCompleted = f.isCompleted,
        isInProgress = f.isInProgress,
        isUpcoming = !f.isLive && !f.isCompleted && !f.isInProgress,
        tossResult = f.tossResult ?: "Toss not yet done",
        resultText = f.resultText,
        innings = f.innings?.map { inning ->
            val team = if (inning.battingTeamId == f.homeTeam?.id) f.homeTeam else f.awayTeam
            inning.toUIInningsData(
                teamName = team?.name ?: "",
                teamShort = team?.shortName ?: "",
                players = players ?: emptyList()
            )
        } ?: emptyList(),
        players = players
    )
}
