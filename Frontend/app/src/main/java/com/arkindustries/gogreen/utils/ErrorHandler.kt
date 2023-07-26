package com.arkindustries.gogreen.utils

import android.util.Log
import com.arkindustries.gogreen.api.response.ApiResponse
import com.google.gson.Gson
import retrofit2.HttpException

suspend fun <T> handleApiCall(operation: suspend () -> ApiResponse<T>): ApiResponse<T> {
    val apiResponse: ApiResponse<T> = try {
        val response = operation ()
        ApiResponse.success(response.data)
    } catch (e: HttpException) {
        val errorResponse = e.response()?.errorBody()?.string()
        val errorData = Gson().fromJson(errorResponse, ApiResponse::class.java)

        ApiResponse.error(errorData.status, errorData.code, errorData.message, errorData.stack)
    } catch (e: Exception) {
        val errorMessage = getHostError(e.message!!) ?: "Something went wrong.Please try again."

        Log.e("Error", e.stackTraceToString())

        ApiResponse.error("error", null, errorMessage, e.stackTraceToString())
    }

    return apiResponse
}

private fun getHostError (errorMessage: String): String? {
    if (errorMessage.contains("Unable to resolve host", true)) {
        return "Failed to connect to the server. Check your internet connection and try again."
    }

    return null
}