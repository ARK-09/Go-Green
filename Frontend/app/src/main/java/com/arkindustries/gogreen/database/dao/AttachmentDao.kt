package com.arkindustries.gogreen.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.arkindustries.gogreen.database.entites.AttachmentEntity

@Dao
interface AttachmentDao {
    @Query("SELECT * FROM attachments WHERE attachmentId = :attachmentId")
    suspend fun getAttachmentById(attachmentId: String): AttachmentEntity

    @Query("SELECT * FROM attachments")
    suspend fun getAllAttachments(): List<AttachmentEntity>

    @Upsert
    suspend fun upsertAttachments(attachments: List<AttachmentEntity>)

    @Query("DELETE FROM attachments WHERE attachmentId = :attachmentId")
    suspend fun deleteById(attachmentId: String)

    @Query("DELETE FROM attachments")
    suspend fun deleteAllAttachments()
}
