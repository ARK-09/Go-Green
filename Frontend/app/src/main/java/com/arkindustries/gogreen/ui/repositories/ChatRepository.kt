package com.arkindustries.gogreen.ui.repositories

import com.arkindustries.gogreen.api.request.AddMembersRequest
import com.arkindustries.gogreen.api.request.CreateRoomRequest
import com.arkindustries.gogreen.api.response.ApiResponse
import com.arkindustries.gogreen.api.response.RoomMemberResponse
import com.arkindustries.gogreen.api.response.RoomMembersResponse
import com.arkindustries.gogreen.api.response.RoomMessagesResponse
import com.arkindustries.gogreen.api.response.RoomResponse
import com.arkindustries.gogreen.api.response.RoomsResponse
import com.arkindustries.gogreen.api.services.ChatService
import com.arkindustries.gogreen.utils.handleApiCall

class ChatRepository(private val chatService: ChatService) {
    suspend fun createRoom(request: CreateRoomRequest): ApiResponse<RoomResponse> {
        return handleApiCall {
            chatService.createRoom(request)
        }
    }

    suspend fun getRooms(offset: Int, limit: Int): ApiResponse<RoomsResponse> {
        return handleApiCall {
            chatService.getRooms(offset, limit)
        }
    }

    suspend fun getRoomById(id: String): ApiResponse<RoomResponse> {
        return handleApiCall {
            chatService.getRoomById(id)
        }
    }

    suspend fun getRoomMessages(id: String, offset: Int, limit: Int): ApiResponse<RoomMessagesResponse> {
        return handleApiCall {
            chatService.getRoomMessages(id, offset, limit)
        }
    }

    suspend fun addMembersToRoom(id: String, request: AddMembersRequest): ApiResponse<RoomResponse> {
        return handleApiCall {
            chatService.addMembersToRoom(id, request)
        }
    }

    suspend fun getRoomMembers(id: String): ApiResponse<RoomMembersResponse> {
        return handleApiCall {
            chatService.getRoomMembers(id)
        }
    }

    suspend fun getRoomMemberById(id: String, memberId: String): ApiResponse<RoomMemberResponse> {
        return handleApiCall {
            chatService.getRoomMemberById(id, memberId)
        }
    }

    suspend fun removeMemberFromRoom(id: String, memberId: String): ApiResponse<Unit> {
        return handleApiCall {
            chatService.removeMemberFromRoom(id, memberId)
        }
    }
}