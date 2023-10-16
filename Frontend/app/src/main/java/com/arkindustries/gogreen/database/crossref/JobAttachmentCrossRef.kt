package com.arkindustries.gogreen.database.crossref

import androidx.room.Entity

@Entity(primaryKeys = ["jobId", "skillId"])
data class JobSkillCrossRef(
    val jobId: String,
    val skillId: String
)