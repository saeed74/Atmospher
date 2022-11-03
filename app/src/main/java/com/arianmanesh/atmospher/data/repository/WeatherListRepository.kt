package com.arianmanesh.atmospher.data.repository

import android.content.Context
import android.util.Log
import com.arianmanesh.atmospher.R
import com.arianmanesh.atmospher.WeatherItemResponse
import com.arianmanesh.atmospher.data.remote.ResponseResult
import com.arianmanesh.atmospher.data.remote.RetrofitInstance
import com.arianmanesh.atmospher.data.database.AtmosphereDataBase
import com.arianmanesh.atmospher.data.model.CitiesDBModel

class WeatherListRepository () {

    suspend fun getCityWeather(
        city: String
    ): ResponseResult<WeatherItemResponse> {

        val response = RetrofitInstance.api.getWeatherDetail(RetrofitInstance.apiKey,city)
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                return ResponseResult.Success(body)
            }
        }
        return ResponseResult.Error(response.code(),response.errorBody())

    }

    suspend fun readAllCitiesFromDB(context: Context): ResponseResult<List<CitiesDBModel>> {
        return ResponseResult.Success(AtmosphereDataBase.getInstance(context).citiesDao().getAllCities())
    }

    suspend fun removeCityFromDB(context: Context, city: CitiesDBModel): ResponseResult<CitiesDBModel> {

        val selectedCity = AtmosphereDataBase.getInstance(context).citiesDao().getCurrentSelectedCity().name
        if(selectedCity == city.name){
            return ResponseResult.DataBaseError(context.getString(R.string.cant_delete_current_city))
        }else{
            AtmosphereDataBase.getInstance(context).citiesDao().deleteCity(city)
            return ResponseResult.Success(city)
        }
    }

    suspend fun getCurrentSelectedCity(context: Context): ResponseResult<CitiesDBModel> {
        return ResponseResult.Success(AtmosphereDataBase.getInstance(context).citiesDao().getCurrentSelectedCity())
    }

    suspend fun unsetLastSelectedCity(context: Context): ResponseResult<Unit> {
        return ResponseResult.Success(AtmosphereDataBase.getInstance(context).citiesDao().unsetLastSelectedCity())
    }

    suspend fun setSelectedCity(city: String, context: Context): ResponseResult<Unit> {
        return ResponseResult.Success(AtmosphereDataBase.getInstance(context).citiesDao().setSelectedCity(city))
    }

}