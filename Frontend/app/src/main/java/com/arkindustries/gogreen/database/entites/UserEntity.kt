package com.arkindustries.gogreen.database.entites

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.arkindustries.gogreen.api.response.Image

@Entity (tableName = "users")
data class UserEntity(
    @PrimaryKey
    val userId: String,
    val name: String,
    val email: String,
    val phoneNo: String?,
    val userType: String,
    @Embedded
    val image: Image?,
    val userStatus: String,
    val verified: Boolean,
    val financeAllowed: Boolean?,
    val blocked: Boolean,
    val blockedReason: String?,
    val joinedDate: String?
)