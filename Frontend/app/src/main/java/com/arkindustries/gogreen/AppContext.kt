package com.arkindustries.gogreen

import com.arkindustries.gogreen.database.entites.UserEntity
import com.arkindustries.gogreen.utils.UserSessionManager

class AppContext private constructor() {
    companion object {
        private lateinit var user: UserEntity
        private lateinit var _userSessionManager: UserSessionManager

        @Volatile
        private var instance: AppContext? = null

        fun initialize(user: UserEntity) {
            if (instance == null) {
                synchronized(AppContext::class.java) {
                    if (instance == null) {
                        init(user)
                        instance = AppContext()
                    }
                }
            }
        }

        fun getInstance(): AppContext {
            if (instance == null) {
                throw IllegalStateException("AppContext must be initialized first.")
            }

            return instance!!
        }

        private fun init (user: UserEntity) {
            _userSessionManager = UserSessionManager
            this.user = user
        }

        fun clear () {
            this.instance = null
        }
    }

    val currentUser: UserEntity
        get() = user

    val userSessionManager: UserSessionManager
        get() = _userSessionManager
}