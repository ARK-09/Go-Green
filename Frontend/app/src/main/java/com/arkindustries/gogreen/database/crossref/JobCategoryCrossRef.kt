package com.arkindustries.gogreen.database.crossref

import androidx.room.Entity

@Entity(tableName = "job_category_cross_ref", primaryKeys = ["jobId", "categoryId"])
data class JobCategoryCrossRef(
    val jobId: String,
    val categoryId: String,
)