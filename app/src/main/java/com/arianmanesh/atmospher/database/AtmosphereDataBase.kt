package com.arianmanesh.atmospher.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CitiesDBModel::class], version = 1)
abstract class AtmosphereDataBase(): RoomDatabase() {

    companion object {
        private const val DB_NAME = "atmosphere_db"
        private var instance: AtmosphereDataBase? = null
        @Synchronized
        open fun getInstance(context: Context): AtmosphereDataBase {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context,
                    AtmosphereDataBase::class.java,
                    DB_NAME
                )
                    .allowMainThreadQueries()
                    .build()
            }
            return instance!!
        }
    }

    abstract fun citiesDao() : CitiesDao

}