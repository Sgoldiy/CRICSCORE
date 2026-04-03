package com.cricpeak.scorelive.ui.apicallfiles


import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST


interface RetrofitInterface {

    @FormUrlEncoded
    @POST("devcount/upcount.php")
    fun updateCounter(
        @Field("packagename") packageName: String,
    ): Call<ResponseBody>
}


object APIClient {

    private const val BASE_URL = "https://quanrix.com/"

    fun getClient(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

object InstallTracker {

    private const val INSTALL_PREF = "install_pref_vd"
    private const val TAG = "InstallTracker"

    fun trackInstall(context: Context) {

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        // ✅ Call only once
        if (prefs.getBoolean(INSTALL_PREF, false)) return

        try {
            val api = APIClient.getClient().create(RetrofitInterface::class.java)

            val call = api.updateCounter(context.packageName)

            call.enqueue(object : Callback<ResponseBody> {

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>,
                ) {
                    if (response.isSuccessful) {

                        Log.d(TAG, "Install tracked successfully")

                        prefs.edit()
                            .putBoolean(INSTALL_PREF, true)
                            .apply()

                    } else {
                        Log.e(TAG, "API failed: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e(TAG, "API error: ${t.message}")
                    call.cancel()
                }
            })

        } catch (e: Exception) {
            Log.e(TAG, "Crash prevented: ${e.message}")
        }
    }
}