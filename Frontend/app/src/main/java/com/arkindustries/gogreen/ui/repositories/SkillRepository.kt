package com.arkindustries.gogreen.ui.repositories

import com.arkindustries.gogreen.api.request.SkillsRequest
import com.arkindustries.gogreen.api.response.ApiResponse
import com.arkindustries.gogreen.api.response.SkillResponse
import com.arkindustries.gogreen.api.response.SkillsResponse
import com.arkindustries.gogreen.api.services.SkillService
import com.arkindustries.gogreen.database.dao.SkillDao
import com.arkindustries.gogreen.database.entites.SkillEntity
import com.arkindustries.gogreen.utils.handleApiCall

class SkillRepository(private val skillService: SkillService, private val skillDao: SkillDao) {

    suspend fun getSkillsFromServer(): ApiResponse<SkillsResponse> {
        return handleApiCall {
          skillService.getSkills()
        }
    }

    suspend fun createSkillAtServer(skillsRequest: SkillsRequest): ApiResponse<SkillResponse> {
        return handleApiCall {
         skillService.createSkill(skillsRequest);
        }
    }

    suspend fun updateSkillAtServer(
        skillId: String,
        skillsRequest: SkillsRequest
    ): ApiResponse<SkillResponse> {
        return handleApiCall {
         skillService.updateSkill(skillId, skillsRequest)
        }
    }

    suspend fun deleteSkillFromServer(skillId: String): ApiResponse<Unit> {
        return handleApiCall {
         skillService.deleteSkill(skillId)
        }
    }

    suspend fun getAllSkillsFromLocal(): List<SkillEntity> {
        return skillDao.getAllSkills()
    }

    suspend fun getSkillByIdFromLocal(skillId: String): SkillEntity {
        return skillDao.getAllSkillById(skillId)
    }

    suspend fun upsertSkillsToLocal(skills: List<SkillEntity>) {
        skillDao.upsertAll(skills)
    }

    suspend fun deleteAllSkillsFromLocal() {
        skillDao.deleteAll()
    }
}