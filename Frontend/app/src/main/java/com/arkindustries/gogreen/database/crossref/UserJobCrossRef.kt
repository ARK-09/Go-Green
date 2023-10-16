package com.arkindustries.gogreen.database.crossref

import androidx.room.Entity

@Entity(tableName = "user_job_cross_ref", primaryKeys = ["userId", "jobId"])
data class UserJobCrossRef(
    val userId: String,
    val jobId: String,
)