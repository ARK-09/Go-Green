package com.arkindustries.gogreen.database.crossref

import androidx.room.Entity

@Entity(primaryKeys = ["jobId", "userId"])
data class JobUserCrossRef(
    val jobId: String,
    val userId: String
)