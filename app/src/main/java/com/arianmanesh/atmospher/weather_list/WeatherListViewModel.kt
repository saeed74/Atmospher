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
import java.net.HttpURLConnection
import java.net.SocketTimeoutException


class WeatherListViewModel(application: Application) : AndroidViewModel(application) {

    private val context : Application = application
    private var toDeletePosition : Int = 0
    //lateinit var deleteJob : Job

    private val _weatherData = MutableLiveData<ResponseResult<WeatherItemResponse>>()
    val weatherData: LiveData<ResponseResult<WeatherItemResponse>>
        get() = _weatherData

    private val _citiesData = MutableLiveData<ResponseResult<List<CitiesDBModel>>>()
    val citiesData: LiveData<ResponseResult<List<CitiesDBModel>>>
        get() = _citiesData

    private val _cityDelete = MutableLiveData<ResponseResult<CitiesDBModel>>()
    val cityDelete: LiveData<ResponseResult<CitiesDBModel>>
        get() = _cityDelete

    private val _currentSelectedCity = MutableLiveData<ResponseResult<CitiesDBModel>>()
    val currentSelectedCity: LiveData<ResponseResult<CitiesDBModel>>
        get() = _currentSelectedCity

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
                    _weatherData.postValue(ResponseResult.Error(HttpURLConnection.HTTP_GATEWAY_TIMEOUT))
                }
            }

        }

    }

    fun fetchAllWeatherListAndCurrentCity(){
        viewModelScope.launch(Dispatchers.IO) {
            val allData = repository.readAllCitiesFromDB(context)
            _citiesData.postValue(allData)
            when (allData) {
                is ResponseResult.Success -> {
                    allData.data?.let {
                        if (it.isNotEmpty()) {
                            _currentSelectedCity.postValue(repository.getCurrentSelectedCity(context))
                        }
                    }
                }
            }
        }
    }

//    suspend fun getCurrentCityFromDB(){
//        _currentSelectedCity.postValue(repository.getCurrentSelectedCity(context))
//    }

    fun unsetLastSelectedCity(){
        viewModelScope.launch(Dispatchers.IO) {
            repository.unsetLastSelectedCity(context)
        }
    }

    fun setSelectedCity(city: String){
        viewModelScope.launch(Dispatchers.IO) {
            repository.setSelectedCity(city, context)
        }
    }

    fun removeCityFromDB(city: CitiesDBModel , pos : Int){
//        if(this::deleteJob.isInitialized && deleteJob.isActive){
//            deleteJob.cancel()
//        }
//        deleteJob = Job()
        viewModelScope.launch(Dispatchers.IO) {
            _cityDelete.postValue(ResponseResult.Loading())
            toDeletePosition = pos
            _cityDelete.postValue(repository.removeCityFromDB(context,city))
        }
    }

    fun getRemovedPosition(): Int{
        return toDeletePosition
    }

}