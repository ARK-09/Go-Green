package com.arkindustries.gogreen.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkindustries.gogreen.api.request.CreateContractRequest
import com.arkindustries.gogreen.api.response.ApiResponse
import com.arkindustries.gogreen.api.response.ContractResponse
import com.arkindustries.gogreen.ui.repositories.ContractRepository
import kotlinx.coroutines.launch

class ContractViewModel(private val repository: ContractRepository) : ViewModel() {
    private val _loadingState = MutableLiveData<Boolean>()
    val loadingState: LiveData<Boolean> get() = _loadingState

    private val _contracts = MutableLiveData<List<ContractResponse>>()
    val contracts: LiveData<List<ContractResponse>> = _contracts

    private val _contract = MutableLiveData<ContractResponse>()
    val contract: LiveData<ContractResponse> = _contract

    private val _error = MutableLiveData<ApiResponse<*>>()
    val error: LiveData<ApiResponse<*>> = _error

    init {
        refreshContracts()
    }

    fun refreshContracts() {
        _loadingState.value = true
        viewModelScope.launch {
            val response = repository.getContractsFromServer()

            if (response.status == "fail" || response.status == "error") {
                _error.value = response
                _loadingState.value = false
                return@launch
            }

            getAllContracts()

            _loadingState.value = false
        }
    }

    fun getAllContracts() {
        _loadingState.value = true
        viewModelScope.launch {
            val response = repository.getContractsFromServer()

            if (response.status == "fail" || response.status == "error") {
                _error.value = response
                _loadingState.value = false
                return@launch
            }

            if (response.data != null) {
                _contracts.value = response.data!!
            }

            _loadingState.value = false
        }
    }

    fun getContractById(contractId: String) {
        _loadingState.value = true
        viewModelScope.launch {
            val response = repository.getContractByIdFromServer(contractId)

            if (response.status == "fail" || response.status == "error") {
                _error.value = response
                _loadingState.value = false
                return@launch
            }
            if (response.data != null) {
                _contract.value = response.data!!
            }

            _loadingState.value = false
        }
    }

    fun deleteContract(contractId: String) {
        _loadingState.value = true
        viewModelScope.launch {
            val response = repository.deleteContractFromServer(contractId)

            if (response.status == "fail" || response.status == "error") {
                _error.value = response
                _loadingState.value = false
                return@launch
            }
            _loadingState.value = false
        }
    }

    fun createContractAtServer(contractRequest: CreateContractRequest) {
        _loadingState.value = true
        viewModelScope.launch {
            val response = repository.createContractAtServer(contractRequest)

            if (response.status == "fail" || response.status == "error") {
                _error.value = response
                _loadingState.value = false
                return@launch
            }

            if (response.data != null) {
                _contract.value = response.data!!
            }
            _loadingState.value = false
        }
    }

    fun updateContractAtServer(contractId: String, contractRequest: CreateContractRequest) {
        _loadingState.value = true
        viewModelScope.launch {
            val response = repository.updateContractAtServer(contractId, contractRequest)

            if (response.status == "fail" || response.status == "error") {
                _error.value = response
                _loadingState.value = false
                return@launch
            }

            if (response.data != null) {
                _contract.value = response.data!!
            }
            _loadingState.value = false
        }
    }

    fun deleteContractFromServer(contractId: String) {
        _loadingState.value = true
        viewModelScope.launch {
            val response = repository.deleteContractFromServer(contractId)

            if (response.status == "fail" || response.status == "error") {
                _loadingState.value = false
                return@launch
            }

            repository.deleteContractFromServer(contractId)
            _loadingState.value = false
        }
    }
}