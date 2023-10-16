package com.arkindustries.gogreen.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkindustries.gogreen.api.request.AddMembersRequest
import com.arkindustries.gogreen.api.request.CreateRoomRequest
import com.arkindustries.gogreen.api.response.ApiResponse
import com.arkindustries.gogreen.api.response.RoomMemberResponse
import com.arkindustries.gogreen.api.response.RoomMembersResponse
import com.arkindustries.gogreen.api.response.RoomMessagesResponse
import com.arkindustries.gogreen.api.response.RoomResponse
import com.arkindustries.gogreen.api.response.RoomsResponse
import com.arkindustries.gogreen.ui.repositories.ChatRepository
import kotlinx.coroutines.launch

class ChatViewModel(private val chatRepository: ChatRepository) : ViewModel() {
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<ApiResponse<*>>()
    val error: LiveData<ApiResponse<*>> get() = _error

    private val _createRoomResponse = MutableLiveData<RoomResponse>()
    val createRoomResponse: LiveData<RoomResponse> get() = _createRoomResponse

    private val _getRoomsResponse = MutableLiveData<RoomsResponse>()
    val getRoomsResponse: LiveData<RoomsResponse> get() = _getRoomsResponse

    private val _getRoomByIdResponse = MutableLiveData<RoomResponse>()
    val getRoomByIdResponse: LiveData<RoomResponse> get() = _getRoomByIdResponse

    private val _getRoomMessagesResponse = MutableLiveData<RoomMessagesResponse>()
    val getRoomMessagesResponse: LiveData<RoomMessagesResponse> get() = _getRoomMessagesResponse

    private val _addMembersToRoomResponse = MutableLiveData<RoomResponse>()
    val addMembersToRoomResponse: LiveData<RoomResponse> get() = _addMembersToRoomResponse

    private val _getRoomMembersResponse = MutableLiveData<RoomMembersResponse>()
    val getRoomMembersResponse: LiveData<RoomMembersResponse> get() = _getRoomMembersResponse

    private val _getRoomMemberByIdResponse = MutableLiveData<RoomMemberResponse>()
    val getRoomMemberByIdResponse: LiveData<RoomMemberResponse> get() = _getRoomMemberByIdResponse

    private val _removeMemberFromRoomResponse = MutableLiveData<Unit>()
    val removeMemberFromRoomResponse: LiveData<Unit> get() = _removeMemberFromRoomResponse

    fun createRoom(request: CreateRoomRequest) {
        _loading.value = true
        viewModelScope.launch {
            val response = chatRepository.createRoom(request)

            if (response.status == "fail" || response.status == "error") {
                _error.value = response
                _loading.value = false
                return@launch
            }

            _createRoomResponse.value = response.data!!
            _loading.value = false
        }
    }

    fun getRooms(offset: Int = 1, limit: Int = 10) {
        _loading.value = true
        viewModelScope.launch {
            val response = chatRepository.getRooms(offset, limit)

            if (response.status == "fail" || response.status == "error") {
                _error.value = response
                _loading.value = false
                return@launch
            }

            _getRoomsResponse.value = response.data!!
            _loading.value = false
        }
    }

    fun getRoomById(id: String) {
        _loading.value = true
        viewModelScope.launch {
            val response = chatRepository.getRoomById(id)

            if (response.status == "fail" || response.status == "error") {
                _error.value = response
                _loading.value = false
                return@launch
            }

            _getRoomByIdResponse.value = response.data!!
            _loading.value = false
        }
    }

    fun getRoomMessages(id: String, offset: Int, limit: Int) {
        _loading.value = true
        viewModelScope.launch {
            val response = chatRepository.getRoomMessages(id, offset, limit)

            if (response.status == "fail" || response.status == "error") {
                _error.value = response
                _loading.value = false
                return@launch
            }

            _getRoomMessagesResponse.value = response.data!!
            _loading.value = false
        }
    }

    fun addMembersToRoom(id: String, request: AddMembersRequest) {
        _loading.value = true
        viewModelScope.launch {
            val response = chatRepository.addMembersToRoom(id, request)

            if (response.status == "fail" || response.status == "error") {
                _error.value = response
                _loading.value = false
                return@launch
            }

            _addMembersToRoomResponse.value = response.data!!
            _loading.value = false
        }
    }

    fun getRoomMembers(id: String) {
        _loading.value = true
        viewModelScope.launch {
            val response = chatRepository.getRoomMembers(id)

            if (response.status == "fail" || response.status == "error") {
                _error.value = response
                _loading.value = false
                return@launch
            }

            _getRoomMembersResponse.value = response.data!!
            _loading.value = false
        }
    }

    fun getRoomMemberById(id: String, memberId: String) {
        _loading.value = true
        viewModelScope.launch {
            val response = chatRepository.getRoomMemberById(id, memberId)

            if (response.status == "fail" || response.status == "error") {
                _error.value = response
                _loading.value = false
                return@launch
            }

            _getRoomMemberByIdResponse.value = response.data!!
            _loading.value = false
        }
    }

    fun removeMemberFromRoom(id: String, memberId: String) {
        _loading.value = true
        viewModelScope.launch {
            val response = chatRepository.removeMemberFromRoom(id, memberId)

            if (response.status == "fail" || response.status == "error") {
                _error.value = response
                _loading.value = false
                return@launch
            }

            _removeMemberFromRoomResponse.value = Unit
            _loading.value = false
        }
    }
}