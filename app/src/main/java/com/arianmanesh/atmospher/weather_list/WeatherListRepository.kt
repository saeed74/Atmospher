package com.arianmanesh.atmospher.weather_list

import android.content.Context
import android.util.Log
import com.arianmanesh.atmospher.WeatherItemResponse
import com.arianmanesh.atmospher.core.ApiService
import com.arianmanesh.atmospher.core.ResponseResult
import com.arianmanesh.atmospher.database.AtmosphereDataBase
import com.arianmanesh.atmospher.database.CitiesDBModel
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class WeatherListRepository () {

    //private val weatherItem: WeatherItemResponse? = null
    private lateinit var retrofit: Retrofit
    private lateinit var apiService : ApiService
    private val apiKey = "db7fff764dff4213a58103931220111"

    suspend fun getCityWeather(
        city: String
    ): ResponseResult<WeatherItemResponse> {

        retrofit = Retrofit.Builder()
            .baseUrl("https://api.weatherapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        val response = apiService.getWeatherDetail(apiKey,city)
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                return ResponseResult.Success(body)
            }
        }
        return ResponseResult.Error(response.errorBody())

    }

    fun readAllCitiesFromDB(context: Context): ResponseResult<List<CitiesDBModel>>{
        return ResponseResult.Success(AtmosphereDataBase.getInstance(context).citiesDao().getAllCities())
    }

    fun removeCityFromDB(context: Context, city: CitiesDBModel){
        ResponseResult.Success(AtmosphereDataBase.getInstance(context).citiesDao().deleteCity(city))
    }

    fun storeCurrentSelectedCity(city: String, context: Context){
        val preference = context.getSharedPreferences("app", Context.MODE_PRIVATE)
        val editor = preference.edit()
        editor.putString("city",city)
        editor.apply()
    }

    fun retrieveCurrentSelectedCity(context: Context): String {
        val preference = context.getSharedPreferences("app", Context.MODE_PRIVATE)
        return preference.getString("city","")!!
    }

}