package com.arianmanesh.atmospher.data.remote

import com.arianmanesh.atmospher.WeatherItemResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @GET("v1/current.json")
    suspend fun getWeatherDetail(
        @Query("key") apiKey: String,
        @Query("q") city: String,
    ): Response<WeatherItemResponse>

}