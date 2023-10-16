package com.arkindustries.gogreen.database.entites

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.arkindustries.gogreen.api.response.Location
import com.arkindustries.gogreen.database.typeconvertor.LocationTypeConverter

@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey
    val jobId: String,
    val title: String,
    val description: String,
    val budget: Double,
    val status: String,
    val expectedDuration: String,
    val paymentType: String,
    @TypeConverters(LocationTypeConverter::class)
    val location: Location,
    val createdDate: String,
    @Embedded
    val user: UserEntity? = null,
    val noOfProposals: Int = 0,
    val interviewing: Int = 0
)