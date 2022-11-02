package com.arianmanesh.atmospher.database

import androidx.room.*

@Dao
interface CitiesDao {

    @Query("SELECT * FROM cities_table")
    fun getAllCities(): List<CitiesDBModel>

    @Query("SELECT EXISTS( SELECT * FROM cities_table WHERE name=:name )")
    fun isCityExist(name: String): Boolean

    @Insert
    fun insertCity(city: CitiesDBModel)

    @Update
    fun updateCity(city: CitiesDBModel)

    @Delete
    fun deleteCity(city: CitiesDBModel)

//    @Query("UPDATE cities_table SET name=:name WHERE id = :id")
//    fun updateStatus(name: String, id: Int)

}