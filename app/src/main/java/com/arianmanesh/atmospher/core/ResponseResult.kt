package com.arianmanesh.atmospher.core

import okhttp3.ResponseBody

sealed class ResponseResult<T> {
    data class Success<T>(val data: T?) : ResponseResult<T>()
    data class Error<T>(val errorResponseBody: ResponseBody?= null) : ResponseResult<T>()
    data class DataBaseError<T>(val error: String) : ResponseResult<T>()
    class Loading<T> : ResponseResult<T>()
}