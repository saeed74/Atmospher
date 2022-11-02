package com.arianmanesh.atmospher.city_modify

import android.content.Context
import android.util.Log
import com.arianmanesh.atmospher.WeatherItemResponse
import com.arianmanesh.atmospher.core.ApiService
import com.arianmanesh.atmospher.core.ResponseResult
import com.arianmanesh.atmospher.database.AtmosphereDataBase
import com.arianmanesh.atmospher.database.CitiesDBModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CityModifyRepository () {

    private lateinit var retrofit: Retrofit
    private lateinit var apiService : ApiService
    private val apiKey = "db7fff764dff4213a58103931220111"

    suspend fun getCityWeather(city: String, modifyMode: Boolean, previousCity: String, context: Context): ResponseResult<WeatherItemResponse> {

        return if (checkCityExist(city, context)){
            ResponseResult.DataBaseError("Repetitive City")
        }else{
            requestByRetrofit(city, modifyMode, previousCity, context)
        }

    }

    private fun checkCityExist(city: String, context: Context) : Boolean{
        return AtmosphereDataBase.getInstance(context).citiesDao().isCityExist(city.lowercase())
    }

    private suspend fun requestByRetrofit(city: String, modifyMode: Boolean, previousCity: String, context: Context) : ResponseResult<WeatherItemResponse>{

        retrofit = Retrofit.Builder()
            .baseUrl("https://api.weatherapi.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        val response = apiService.getWeatherDetail(apiKey,city)
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                //Save to DataBase and change current selected city
                val citiesDBModel = convertToDBModel(body);
                if(modifyMode){
                    val id = AtmosphereDataBase.getInstance(context).citiesDao().findIdByName(previousCity)
                    AtmosphereDataBase.getInstance(context).citiesDao().updateCityById(citiesDBModel.name,id)
                }else{
                    AtmosphereDataBase.getInstance(context).citiesDao().insertCity(citiesDBModel)
                }
                AtmosphereDataBase.getInstance(context).citiesDao().unsetLastSelectedCity()
                AtmosphereDataBase.getInstance(context).citiesDao().setSelectedCity(body.location.name.lowercase())
                return ResponseResult.Success(body)
            }
        }
        return ResponseResult.Error(response.errorBody())
    }

    private fun convertToDBModel(body: WeatherItemResponse): CitiesDBModel{
        val cityItem = CitiesDBModel()
        cityItem.name = body.location.name.lowercase()
        cityItem.country = body.location.country
        cityItem.icon = body.current.condition.icon
        cityItem.temp_c = body.current.temp_c
        cityItem.last_updated = body.current.last_updated
        return cityItem
    }

    //todo: mig to db
//    private fun storeCurrentSelectedCity(city: String, context: Context){
//        val preference = context.getSharedPreferences("app", Context.MODE_PRIVATE)
//        val editor = preference.edit()
//        editor.putString("city",city)
//        editor.apply()
//    }

}