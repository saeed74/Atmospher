package com.arianmanesh.atmospher.weather_list

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.arianmanesh.atmospher.WeatherItemResponse
import com.arianmanesh.atmospher.core.ResponseResult
import com.arianmanesh.atmospher.database.CitiesDBModel
import kotlinx.coroutines.*
import java.net.SocketTimeoutException


class WeatherListViewModel(application: Application) : AndroidViewModel(application) {

    private val context : Application = application

    private val _weatherData = MutableLiveData<ResponseResult<WeatherItemResponse>>()
    val weatherData: LiveData<ResponseResult<WeatherItemResponse>>
        get() = _weatherData

    private val _citiesData = MutableLiveData<ResponseResult<List<CitiesDBModel>>>()
    val citiesData: LiveData<ResponseResult<List<CitiesDBModel>>>
        get() = _citiesData

    private val repository = WeatherListRepository()

    fun updateWeather(city: String) {

        viewModelScope.launch(Dispatchers.IO) {

            try {
                _weatherData.postValue(ResponseResult.Loading())
                when (val result = repository.getCityWeather(city)){
                    is ResponseResult.Success ->{
                        _weatherData.postValue(result)
                    }
                    is ResponseResult.Error ->{
                        _weatherData.postValue(result)
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                if(e is SocketTimeoutException) {
                    _weatherData.postValue(ResponseResult.Error(null))
                }
            }

        }

    }

    fun getAllCitiesFromDB(){
        viewModelScope.launch(Dispatchers.IO) {
            _citiesData.postValue(ResponseResult.Loading())
            _citiesData.postValue(repository.readAllCitiesFromDB(context))
        }
    }

    fun removeCityFromDB(city: CitiesDBModel){
        viewModelScope.launch(Dispatchers.IO) {
            _citiesData.postValue(ResponseResult.Loading())
            repository.removeCityFromDB(context,city);
            _citiesData.postValue(repository.readAllCitiesFromDB(context))
        }
    }

    fun retrieveCurrentSelectedCity(context: Context): String{
        return repository.retrieveCurrentSelectedCity(context)
    }

    fun storeCurrentSelectedCity(city: String,context: Context){
        repository.storeCurrentSelectedCity(city,context)
    }



}