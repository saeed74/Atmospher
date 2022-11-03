package com.arianmanesh.atmospher.data.repository

import android.content.Context
import com.arianmanesh.atmospher.WeatherItemResponse
import com.arianmanesh.atmospher.data.remote.ResponseResult
import com.arianmanesh.atmospher.data.remote.RetrofitInstance
import com.arianmanesh.atmospher.data.database.AtmosphereDataBase
import com.arianmanesh.atmospher.data.model.CitiesDBModel

class CityModifyRepository () {


    suspend fun getCityWeather(city: String, modifyMode: Boolean, previousCity: String, context: Context): ResponseResult<WeatherItemResponse> {

        return if (checkCityExist(city, context)){
            ResponseResult.DataBaseError("Repetitive City")
        }else{
            requestByRetrofit(city, modifyMode, previousCity, context)
        }

    }

    private suspend fun checkCityExist(city: String, context: Context) : Boolean{
        return AtmosphereDataBase.getInstance(context).citiesDao().isCityExist(city.lowercase())
    }

    private suspend fun requestByRetrofit(city: String, modifyMode: Boolean, previousCity: String, context: Context) : ResponseResult<WeatherItemResponse> {

        val response = RetrofitInstance.api.getWeatherDetail(RetrofitInstance.apiKey,city)
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                //Save to DataBase and change current selected city
                val citiesDBModel = convertToDBModel(body)
                if(modifyMode){
                    val id = AtmosphereDataBase.getInstance(context).citiesDao().findIdByName(previousCity)
                    AtmosphereDataBase.getInstance(context).citiesDao().updateCityById(citiesDBModel.country,citiesDBModel.name,id)
                }else{
                    AtmosphereDataBase.getInstance(context).citiesDao().insertCity(citiesDBModel)
                }
                AtmosphereDataBase.getInstance(context).citiesDao().unsetLastSelectedCity()
                AtmosphereDataBase.getInstance(context).citiesDao().setSelectedCity(body.location.name.lowercase())
                return ResponseResult.Success(body)
            }
        }
        return ResponseResult.Error(response.code(),response.errorBody())
    }

    private fun convertToDBModel(body: WeatherItemResponse): CitiesDBModel {
        val cityItem = CitiesDBModel()
        cityItem.name = body.location.name.lowercase()
        cityItem.country = body.location.country
        cityItem.icon = body.current.condition.icon
        cityItem.temp_c = body.current.temp_c
        cityItem.wind = body.current.wind_kph
        cityItem.humidity = body.current.humidity
        cityItem.last_updated = body.current.last_updated
        return cityItem
    }

}