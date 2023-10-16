package com.arkindustries.gogreen.database.typeconvertor

import androidx.room.TypeConverter
import com.arkindustries.gogreen.api.response.Location

class LocationTypeConverter {
    @TypeConverter
    fun fromLocation(location: Location): String {
        return (location.coordinates?.get(0) ?: "") +", "+ (location.coordinates?.get(1) ?: "")
    }

    @TypeConverter
    fun toLocation(location: String): Location {
        val locationCords = location.split(",");

        return Location("Point", listOf(locationCords[0], locationCords[1]))

    }
}
