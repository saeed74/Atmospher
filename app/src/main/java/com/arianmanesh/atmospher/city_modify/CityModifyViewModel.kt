package com.arianmanesh.atmospher.city_modify

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.arianmanesh.atmospher.WeatherItemResponse
import com.arianmanesh.atmospher.core.ResponseResult
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.IDN
import java.net.SocketTimeoutException
import kotlin.math.log

class CityModifyViewModel(application: Application) : AndroidViewModel(application) {

    private val _weatherData = MutableLiveData<ResponseResult<WeatherItemResponse>>()
    val weatherData: LiveData<ResponseResult<WeatherItemResponse>>
        get() = _weatherData
    private val repository = CityModifyRepository()
    private val context : Application = application

    fun updateWeather(city: String, modifyMode: Boolean, previousCity: String) {
        viewModelScope.launch(Dispatchers.IO) {

            try {
                _weatherData.postValue(ResponseResult.Loading())
                when (val result = repository.getCityWeather(city, modifyMode, previousCity, context)) {
                    is ResponseResult.Success -> {
                        _weatherData.postValue(result)
                    }
                    is ResponseResult.Error -> {
                        _weatherData.postValue(result)
                    }
                    is ResponseResult.DataBaseError -> {
                        _weatherData.postValue(result)
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e("TAG_Exception", e.message.toString())
                if (e is SocketTimeoutException) {
                    _weatherData.postValue(ResponseResult.Error(HttpURLConnection.HTTP_GATEWAY_TIMEOUT,null))
                }
            }

        }

    }

}