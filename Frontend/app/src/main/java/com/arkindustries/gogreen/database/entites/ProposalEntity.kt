package com.arkindustries.gogreen.database.entites

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "proposals")
data class ProposalEntity(
    @PrimaryKey
    val proposalId: String,
    val doc: String,
    val refType: String, // Type of the associated reference (e.g., "job" or "service")
    val bidAmount: Double,
    val status: String,
    val coverLetter: String,
    val proposedDuration: String,
    val clientFeedback: String?,
    val talentFeedback: String?,
    val clientRating: Double?,
    val talentRating: Double?,
    @Embedded(prefix = "user_")
    val user: UserEntity? = null
)
