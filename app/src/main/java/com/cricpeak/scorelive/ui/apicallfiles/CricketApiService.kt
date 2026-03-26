package com.cricpeak.scorelive.ui.apicallfiles

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

interface CricketApiService {
    @GET("mobile/views/fixtures")
    suspend fun getFixtures(
        @Query("UpcomingFixturesCount") upcomingCount: Int = 30,
        @Query("InProgressFixturesCount") inProgressCount: Int = 30,
        @Query("CompletedFixturesCount") completedCount: Int = 30
    ): FixturesResponse

    @GET("mobile/views/matches/{matchId}")
    suspend fun getMatchDetails(
        @Path("matchId") matchId: Int
    ): MatchDetailsResponse

    @GET("mobile/views/scorecard")
    suspend fun getScorecard(
        @Query("FixtureId") fixtureId: Int,
        @Query("InningNumber") inningNumber: Int? = null
    ): ScorecardResponse

    @GET("mobile/views/comments")
    suspend fun getComments(
        @Query("FixtureId") fixtureId: Int,
        @Query("InningNumber") inningNumber: Int = 1,
        @Query("CompletedOversCount") completedOversCount: Int = 500,
        @Query("IncludePastOvers") includePastOvers: Boolean = true,
        @Query("IncludePreviousOvers") includePreviousOvers: Boolean = true
    ): CommentsResponse

    companion object {
        private const val BASE_URL = "https://apiv2.cricket.com.au/"

        fun create(): CricketApiService {
            // Logging interceptor
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            // Simple header interceptor to ensure required headers are sent
            val headerInterceptor = Interceptor { chain ->
                val original: Request = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                val request = requestBuilder.build()
                chain.proceed(request)
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(headerInterceptor)
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(CricketApiService::class.java)
        }
    }
}
