package com.arkindustries.gogreen.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkindustries.gogreen.api.request.SkillsRequest
import com.arkindustries.gogreen.api.response.ApiResponse
import com.arkindustries.gogreen.database.entites.SkillEntity
import com.arkindustries.gogreen.ui.repositories.SkillRepository
import kotlinx.coroutines.launch

class SkillViewModel(private val repository: SkillRepository) : ViewModel() {
    private val _loadingState = MutableLiveData<Boolean>()
    val loadingState: LiveData<Boolean> get() = _loadingState

    private val _skills = MutableLiveData<List<SkillEntity>>()
    val skills: LiveData<List<SkillEntity>> = _skills

    private val _skill = MutableLiveData<SkillEntity>()
    val skill: LiveData<SkillEntity> = _skill

    private val _error = MutableLiveData<ApiResponse<*>>()
    val error: LiveData<ApiResponse<*>> = _error

    init {
        refreshSkills()
    }

    fun refreshSkills() {
        _loadingState.value = true
        viewModelScope.launch {
            val response = repository.getSkillsFromServer()

            if (response.status == "fail" || response.status == "error") {
                _error.value = response
                _loadingState.value = false
                return@launch
            }

            val skills = response.data?.skills?.map { skill ->
                return@map SkillEntity(skill._id, skill.title)
            }

            if (skills != null) {
                repository.upsertSkillsToLocal(skills)
                getAllSkills()
            }

            _loadingState.value = false
        }
    }

    fun getAllSkills() {
        _loadingState.value = true
        viewModelScope.launch {
            _skills.value = repository.getAllSkillsFromLocal()
            _loadingState.value = false
        }
    }

    fun getSkillById(skillId: String) {
        _loadingState.value = true
        viewModelScope.launch {
            _skill.value = repository.getSkillByIdFromLocal(skillId)
            _loadingState.value = false
        }
    }

    fun deleteAllSkills() {
        _loadingState.value = true
        viewModelScope.launch {
            repository.deleteAllSkillsFromLocal()
            _loadingState.value = false
        }
    }

    fun createSkillFromServer(skillsRequest: SkillsRequest) {
        _loadingState.value = true
        viewModelScope.launch {
            val response = repository.createSkillAtServer(skillsRequest)

            if (response.status == "fail" || response.status == "error") {
                _error.value = response
                _loadingState.value = false
                return@launch
            }

            val skills = mutableListOf<SkillEntity>()
            response.data?.let {
                skills.add(SkillEntity(it.skill._id, response.data.skill.title))
            }

            repository.upsertSkillsToLocal(skills)
            _loadingState.value = false
        }
    }

    fun updateSkillAtServer(skillId: String, skillsRequest: SkillsRequest) {
        _loadingState.value = true
        viewModelScope.launch {
            val response = repository.updateSkillAtServer(skillId, skillsRequest)

            if (response.status == "fail" || response.status == "error") {
                _error.value = response
                _loadingState.value = false
                return@launch
            }

            if (response.data != null) {
                repository.upsertSkillsToLocal(
                    mutableListOf(
                        SkillEntity(
                            response.data.skill._id,
                            response.data.skill.title
                        )
                    )
                )
                _skill.value = repository.getSkillByIdFromLocal(response.data.skill._id)
            }
            _loadingState.value = false
        }
    }

    fun deleteSkillFromServer(skillId: String) {
        _loadingState.value = true
        viewModelScope.launch {
            val response = repository.deleteSkillFromServer(skillId)

            if (response.status == "fail" || response.status == "error") {
                _error.value = response
                _loadingState.value = false
                return@launch
            }
            repository.deleteSkillFromServer(skillId)
            _loadingState.value = false
        }
    }
}
