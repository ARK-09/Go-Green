package com.arkindustries.gogreen.api.services

import com.arkindustries.gogreen.api.request.SkillsRequest
import com.arkindustries.gogreen.api.response.ApiResponse
import com.arkindustries.gogreen.api.response.SkillResponse
import com.arkindustries.gogreen.api.response.SkillsResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface SkillService {
    @GET("skills")
    suspend fun getSkills(): ApiResponse<SkillsResponse>

    @POST("skills")
    suspend fun createSkill(@Body request: SkillsRequest): ApiResponse<SkillResponse>

    @PATCH("skills/{id}")
    suspend fun updateSkill(
        @Path("id") skillId: String,
        @Body request: SkillsRequest
    ): ApiResponse<SkillResponse>

    @DELETE("skills/{id}")
    suspend fun deleteSkill(@Path("id") skillId: String): ApiResponse<Unit>
}
