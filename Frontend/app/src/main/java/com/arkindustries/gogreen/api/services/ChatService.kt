package com.arkindustries.gogreen.api.services

import com.arkindustries.gogreen.api.request.AddMembersRequest
import com.arkindustries.gogreen.api.request.CreateRoomRequest
import com.arkindustries.gogreen.api.response.ApiResponse
import com.arkindustries.gogreen.api.response.RoomMemberResponse
import com.arkindustries.gogreen.api.response.RoomMembersResponse
import com.arkindustries.gogreen.api.response.RoomMessagesResponse
import com.arkindustries.gogreen.api.response.RoomResponse
import com.arkindustries.gogreen.api.response.RoomsResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatService {
    @POST("chats/rooms")
    suspend fun createRoom(@Body request: CreateRoomRequest): ApiResponse<RoomResponse>

    @GET("chats/rooms")
    suspend fun getRooms(
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): ApiResponse<RoomsResponse>

    @GET("chats/rooms/{id}")
    suspend fun getRoomById(@Path("id") id: String): ApiResponse<RoomResponse>

    @GET("chats/rooms/{id}/messages")
    suspend fun getRoomMessages(
        @Path("id") id: String,
        @Query("offset") offset: Int,
        @Query("limit") limit: Int
    ): ApiResponse<RoomMessagesResponse>

    @POST("chats/rooms/{id}/members")
    suspend fun addMembersToRoom(
        @Path("id") id: String,
        @Body request: AddMembersRequest
    ): ApiResponse<RoomResponse>

    @GET("chats/rooms/{id}/members")
    suspend fun getRoomMembers(@Path("id") id: String): ApiResponse<RoomMembersResponse>

    @GET("chats/rooms/{id}/members/{memberId}")
    suspend fun getRoomMemberById(
        @Path("id") id: String,
        @Path("memberId") memberId: String
    ): ApiResponse<RoomMemberResponse>

    @DELETE("chats/rooms/{id}/members/{memberId}")
    suspend fun removeMemberFromRoom(
        @Path("id") id: String,
        @Path("memberId") memberId: String
    ): ApiResponse<Unit>
}
