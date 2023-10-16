package com.arkindustries.gogreen.api.services

import com.arkindustries.gogreen.api.response.ApiResponse
import com.arkindustries.gogreen.api.response.FileResponse
import com.arkindustries.gogreen.api.response.FilesResponse
import okhttp3.MultipartBody
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface FileService {
    @Multipart
    @POST("files")
    suspend fun uploadFiles(@Part files: List<MultipartBody.Part>): ApiResponse<FilesResponse>

    @GET("files/{id}")
    suspend fun getFileById(@Path("id") fileId: String): ApiResponse<FileResponse>

    @DELETE("files/{id}")
    suspend fun deleteFileById(@Path("id") fileId: String): ApiResponse<String>
}
