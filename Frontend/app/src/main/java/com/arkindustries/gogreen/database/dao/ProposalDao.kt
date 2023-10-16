package com.arkindustries.gogreen.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.arkindustries.gogreen.database.crossref.ProposalJobCrossRef
import com.arkindustries.gogreen.database.entites.ProposalEntity
import com.arkindustries.gogreen.database.entites.ProposalWithAttachments
import com.arkindustries.gogreen.database.entites.ProposalWithAttachmentsAndJob
import com.arkindustries.gogreen.database.entites.ProposalWithJob

@Dao
interface ProposalDao {
    @Upsert
    suspend fun upsertProposals(proposal: List<ProposalEntity>)

    @Upsert
    suspend fun upsertProposalWithJob(proposalWithJob: List<ProposalJobCrossRef>)

    @Query("DELETE FROM proposals")
    suspend fun deleteAll()

    @Query("DELETE from proposal_job_cross_ref")
    suspend fun deleteProposalJobCrossRef()

    @Query("DELETE from user_proposal_cross_ref")
    suspend fun deleteProposalUserCrossRef()

    @Query("DELETE FROM proposals WHERE proposalId= :proposalId")
    suspend fun deleteById(proposalId: String)

    @Query("DELETE FROM proposal_attachment_cross_ref WHERE proposalId= :proposalId AND attachmentId= :attachmentId")
    suspend fun deleteProposalAttachmentCrossRef(proposalId: String, attachmentId: String)

    @Transaction
    @Query("SELECT * FROM proposals WHERE doc= :jobId")
    suspend fun getJobProposalsWithAttachments(jobId: String): List<ProposalWithAttachments>

    @Transaction
    @Query("SELECT * FROM proposals WHERE proposalId= :proposalId")
    suspend fun getProposalWithJob(proposalId: String): List<ProposalWithJob>

    @Transaction
    @Query("SELECT * FROM proposals WHERE doc= :jobId")
    suspend fun getJobProposalsWithAttachmentsAndJob(jobId: String): List<ProposalWithAttachmentsAndJob>

    @Transaction
    @Query("SELECT * FROM proposals WHERE user_userId= :userId")
    suspend fun getProposalsByUser(userId: String): List<ProposalWithAttachmentsAndJob>

    @Transaction
    @Query("SELECT * FROM proposals WHERE proposalId = :proposalId")
    suspend fun getProposalById(proposalId: String): ProposalWithAttachmentsAndJob?
}