package com.arkindustries.gogreen.database.entites

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity (tableName = "skills")
class SkillEntity(
    @PrimaryKey
    val skillId: String,
    val title: String
)