package com.arkindustries.gogreen.api.response

data class ApiResponse<T>(
    val status: String?,
    val code: Int?,
    val message: String?,
    val stack: String?,
    val data: T?
) {
    companion object {
        fun <T> success(data: T?): ApiResponse<T> {
            return ApiResponse("success", 200, null, null, data)
        }

        fun <T> error(status: String?, code: Int?, message: String?, stack: String?): ApiResponse<T> {
            return ApiResponse(status, code, message, stack, null)
        }
    }
}

