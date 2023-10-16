package com.arkindustries.gogreen.ui.repositories

import com.arkindustries.gogreen.api.request.CreateContractRequest
import com.arkindustries.gogreen.api.response.ApiResponse
import com.arkindustries.gogreen.api.response.ContractResponse
import com.arkindustries.gogreen.api.services.ContractsService
import com.arkindustries.gogreen.utils.handleApiCall

class ContractRepository(
    private val contractService: ContractsService
) {

    suspend fun getContractsFromServer(): ApiResponse<List<ContractResponse>> {
        return handleApiCall {
            contractService.getContracts()
        }
    }

    suspend fun getContractByIdFromServer(contractId: String): ApiResponse<ContractResponse> {
        return handleApiCall {
            contractService.getContractById(contractId)
        }
    }

    suspend fun createContractAtServer(contractRequest: CreateContractRequest): ApiResponse<ContractResponse> {
        return handleApiCall {
            contractService.createContract(contractRequest)
        }
    }

    suspend fun updateContractAtServer(
        contractId: String,
        contractRequest: CreateContractRequest
    ): ApiResponse<ContractResponse> {
        return handleApiCall {
            contractService.updateContract(contractId, contractRequest)
        }
    }

    suspend fun deleteContractFromServer(contractId: String): ApiResponse<Unit> {
        return handleApiCall {
            contractService.deleteContract(contractId)
        }
    }
}