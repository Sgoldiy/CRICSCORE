package com.cricpeak.scorelive.ui.apicallfiles

import java.text.SimpleDateFormat
import java.util.*

data class MatchInfo(
    val team1Short: String,
    val team1Full: String,
    val team2Short: String,
    val team2Full: String,
    val date: String,
    val venue: String,
    val toss: String,
    val umpire: String = "",
    val tvUmpire: String = "",
    val matchReferee: String = ""
)

/**
 * Converts GMT/UTC time string to user's local timezone
 * Compatible with API level 24+
 * @param gmtTimeString Time in format: "2026-03-17T06:15:00Z"
 * @return Formatted local time string: "17 Mar, 11:45 AM"
 */
fun String.convertGMTToLocal(): String {
    return try {
        if (this.isEmpty()) return ""
        
        // Parse UTC time
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(this) ?: return this
        
        // Convert to local time
        val outputFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
        outputFormat.timeZone = TimeZone.getDefault()
        outputFormat.format(date)
    } catch (e: Exception) {
        // Fallback: return original string if parsing fails
        this
    }
}

/**
 * Extension function to create MatchInfo from MatchDetailsData
 * Maps API data to UI-friendly format
 */
fun MatchDetailsData.toMatchInfo(): MatchInfo {
    return MatchInfo(
        team1Short = homeTeam.shortName,
        team1Full = homeTeam.name,
        team2Short = awayTeam.shortName,
        team2Full = awayTeam.name,
        date = startDateTime,
        venue = "${venue.name}, ${venue.city}",
        toss = tossResult,
        umpire = "",
        tvUmpire = "",
        matchReferee = ""
    )
}

/**
 * Get first inning data safely
 */
fun MatchDetailsData.getFirstInning(): InningData? = innings.getOrNull(0)

/**
 * Get second inning data safely
 */
fun MatchDetailsData.getSecondInning(): InningData? = innings.getOrNull(1)

/**
 * Check if match has completed
 */
fun MatchDetailsData.isMatchFinished(): Boolean = isCompleted

/**
 * Check if match is currently live
 */
fun MatchDetailsData.isMatchLive(): Boolean = isLive

/**
 * Check if match is upcoming
 */
fun MatchDetailsData.isMatchUpcoming(): Boolean = isUpcoming

/**
 * Get winner team data
 */
fun MatchDetailsData.getWinnerTeam(): TeamData? {
    return if (homeTeam.isMatchWinner) homeTeam else if (awayTeam.isMatchWinner) awayTeam else null
}

/**
 * Get batting team for a specific inning
 */
fun MatchDetailsData.getBattingTeam(inning: InningData): TeamData? {
    return when (inning.battingTeamId) {
        homeTeam.id -> homeTeam
        awayTeam.id -> awayTeam
        else -> null
    }
}


