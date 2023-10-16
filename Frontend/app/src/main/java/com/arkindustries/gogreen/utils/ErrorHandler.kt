package com.arkindustries.gogreen.utils

import android.util.Log
import com.arkindustries.gogreen.api.response.ApiResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.HttpException

suspend fun <T> handleApiCall(operation: suspend () -> ApiResponse<T>): ApiResponse<T> {
    val apiResponse: ApiResponse<T> = try {
        val response = operation ()
        ApiResponse.success(response.data)
    } catch (e: HttpException) {
        val errorResponse = e.response()?.errorBody()?.string()
        val errorData = errorResponse?.let { parseResponse<T>(it) }

        if (errorData != null) {
            ApiResponse.error(errorData.status, errorData.code, errorData.message, errorData.stack)
        } else {
            ApiResponse.error("error", null, "Something went wrong.Please try again.", e.stackTraceToString())
        }
    } catch (e: Exception) {
        val errorMessage = getHostError(e.message) ?: "Something went wrong.Please try again."

        Log.e("Error", e.stackTraceToString())
        ApiResponse.error("error", null, errorMessage, e.stackTraceToString())
    }

    return apiResponse
}

private fun <T> parseResponse(errorResponse: String): ApiResponse<T> {
    return try {
        val type = object : TypeToken<ApiResponse<T>>() {}.type
        Gson().fromJson(errorResponse, type)
    } catch (e: Exception) {
        ApiResponse.error("error", null, "Something went wrong. Please try again.", e.stackTraceToString())
    }
}

private fun getHostError (errorMessage: String?): String? {
    if (!errorMessage.isNullOrEmpty()) {
        if (errorMessage.contains("Unable to resolve host", true)) {
            return "Failed to connect to the server. Check your internet connection and try again."
        }
    }
    return null
}