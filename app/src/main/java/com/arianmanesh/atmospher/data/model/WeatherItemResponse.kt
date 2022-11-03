package com.arianmanesh.atmospher

data class WeatherItemResponse (
    val location: Location,
    val current: Current
)

data class Location (
    val name: String,
    val region: String,
    val country: String,
    val lat: Double,
    val lon: Double
)

data class Current (
    val last_updated: String,
    val temp_c: Double,
    val wind_kph: Double,
    val humidity: Int,
    val condition: Condition
)

data class Condition (
    val text: String,
    val icon: String
)