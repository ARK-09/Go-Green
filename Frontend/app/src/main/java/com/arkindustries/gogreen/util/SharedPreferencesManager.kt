package com.arkindustries.gogreen.util

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_AUTHORIZATION, Context.MODE_PRIVATE)

    fun saveJwtToken(token: String) {
        sharedPreferences.edit().putString(KEY_JWT_TOKEN, token).apply()
    }

    fun getJwtToken(): String? {
        return sharedPreferences.getString(KEY_JWT_TOKEN, null)
    }

    companion object {
        private const val PREFS_AUTHORIZATION = "Authorization"
        private const val KEY_JWT_TOKEN = "jwt_token"
    }
}

