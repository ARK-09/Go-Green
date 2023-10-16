package com.arkindustries.gogreen.database.entites

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
class CategoryEntity(
    @PrimaryKey
    val categoryId: String,
    val title: String
)
