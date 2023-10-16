package com.arkindustries.gogreen.utils

import android.content.Context
import android.util.Log

object UserSessionManager {
    private const val PREF_NAME = "user_session"
    private const val KEY_JWT_TOKEN = "jwt_token"

    fun saveJwtToken(context: Context, token: String) {
        val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        preferences.edit().clear().apply()
        Log.i(UserSessionManager::class.java.simpleName, preferences.getString(KEY_JWT_TOKEN, "default").toString())
        preferences.edit().putString(KEY_JWT_TOKEN, token).apply()
    }

    fun getJwtToken(context: Context): String? {
        val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return preferences.getString(KEY_JWT_TOKEN, null)
    }

    fun clearJwtToken(context: Context) {
        val preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        preferences.edit().remove(KEY_JWT_TOKEN).apply()
    }
}



