package com.arkindustries.gogreen.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.arkindustries.gogreen.database.entites.SkillEntity

@Dao
interface SkillDao {
    @Query("SELECT * FROM skills")
    suspend fun getAllSkills(): List<SkillEntity>

    @Query("SELECT * FROM skills WHERE skillId= :skillId")
    suspend fun getAllSkillById(skillId: String): SkillEntity

    @Upsert
    suspend fun upsertAll(skills: List<SkillEntity>)

    @Query("DELETE FROM skills")
    suspend fun deleteAll()

    @Query("DELETE FROM skills WHERE skillId= :skillId")
    suspend fun deleteById(skillId: String)
}
