package com.arianmanesh.atmospher.data.database

import androidx.room.*
import com.arianmanesh.atmospher.data.model.CitiesDBModel

@Dao
interface CitiesDao {

    @Query("SELECT * FROM cities_table")
    suspend fun getAllCities(): List<CitiesDBModel>

    @Query("SELECT EXISTS( SELECT * FROM cities_table WHERE name=:name )")
    suspend fun isCityExist(name: String): Boolean

    @Insert
    suspend fun insertCity(city: CitiesDBModel)

    @Update
    suspend fun updateCity(city: CitiesDBModel)

    @Delete
    suspend fun deleteCity(city: CitiesDBModel)

    @Query("SELECT id FROM cities_table WHERE name=:name")
    suspend fun findIdByName(name: String): Int

    @Query("UPDATE cities_table SET name=:name, country=:country WHERE id = :id")
    suspend fun updateCityById(country: String, name: String, id: Int)

    @Query("UPDATE cities_table SET selected = 1 WHERE name = :name")
    suspend fun setSelectedCity(name: String)

    @Query("UPDATE cities_table SET selected = 0 WHERE selected = 1")
    suspend fun unsetLastSelectedCity()

    @Query("SELECT EXISTS( SELECT * FROM cities_table WHERE selected = 1 )")
    suspend fun isAnyCityAlreadySelected(): Boolean

    @Query("SELECT * FROM cities_table WHERE selected = 1")
    suspend fun getCurrentSelectedCity(): CitiesDBModel

//    @Query("DELETE FROM cities_table WHERE id=:id")
//    fun deleteCity(id: Int) : CitiesDBModel

}