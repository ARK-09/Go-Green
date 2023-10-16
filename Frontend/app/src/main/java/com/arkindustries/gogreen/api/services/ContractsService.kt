package com.arkindustries.gogreen.api.services

import com.arkindustries.gogreen.api.request.CreateContractRequest
import com.arkindustries.gogreen.api.response.ApiResponse
import com.arkindustries.gogreen.api.response.ContractResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ContractsService {
    @POST("contracts")
    suspend fun createContract(@Body request: CreateContractRequest): ApiResponse<ContractResponse>

    @GET("contracts")
    suspend fun getContracts(): ApiResponse<List<ContractResponse>>

    @GET("contracts/{id}")
    suspend fun getContractById(@Path("id") contractId: String): ApiResponse<ContractResponse>

    @PATCH("contracts/{id}")
    suspend fun updateContract(
        @Path("id") contractId: String,
        @Body request: CreateContractRequest
    ): ApiResponse<ContractResponse>

    @DELETE("contracts/{id}")
    suspend fun deleteContract(@Path("id") contractId: String): ApiResponse<Unit>
}
