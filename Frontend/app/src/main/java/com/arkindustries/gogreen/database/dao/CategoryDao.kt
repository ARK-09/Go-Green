package com.arkindustries.gogreen.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.arkindustries.gogreen.database.entites.CategoryEntity

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories")
    suspend fun getAllCategories(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE categoryId= :categoryId")
    suspend fun getCategoryById(categoryId: String): CategoryEntity

    @Upsert
    suspend fun upsertAll(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories")
    suspend fun deleteAll()

    @Query("DELETE FROM categories WHERE categoryId= :categoryId")
    suspend fun deleteById(categoryId: String)
}
