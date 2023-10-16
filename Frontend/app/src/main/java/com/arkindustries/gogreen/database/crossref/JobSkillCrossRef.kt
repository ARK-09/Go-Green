package com.arkindustries.gogreen.database.crossref

import androidx.room.Entity

@Entity(tableName= "job_skill_cross_ref", primaryKeys = ["jobId", "skillId"])
data class JobSkillCrossRef(
    val jobId: String,
    val skillId: String
)