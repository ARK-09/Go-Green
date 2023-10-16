package com.arkindustries.gogreen.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.arkindustries.gogreen.database.entites.UserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUserById(userId: String): UserEntity

    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<UserEntity>

    @Upsert
    suspend fun upsertUser(user: List<UserEntity>)

    @Query("DELETE FROM users WHERE userId= :userId")
    suspend fun deleteById(userId: String)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()
}