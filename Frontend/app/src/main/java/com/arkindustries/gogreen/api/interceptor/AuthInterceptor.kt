package com.arkindustries.gogreen.api.interceptor

import android.content.Context
import com.arkindustries.gogreen.utils.UserSessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Skip adding token to login and signup routes
        if (request.url.encodedPathSegments.contains("login") ||
            request.url.encodedPathSegments.contains("signup")
        ) {
            return chain.proceed(request)
        }

        val token = UserSessionManager.getJwtToken(context)
        val authenticatedRequest = if (!token.isNullOrEmpty()) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }

        return chain.proceed(authenticatedRequest)
    }
}
