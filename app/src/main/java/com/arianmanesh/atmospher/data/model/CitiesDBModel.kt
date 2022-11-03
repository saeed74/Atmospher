package com.arianmanesh.atmospher.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cities_table")
class CitiesDBModel() {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
    var country: String = ""
    var name: String = ""
    var last_updated: String = ""
    var temp_c: Double = 0.0
    var icon: String = ""
    var selected: Int = 0
}