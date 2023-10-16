package com.arkindustries.gogreen.ui.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.arkindustries.gogreen.api.RetrofitClient
import com.arkindustries.gogreen.api.services.FileService
import com.arkindustries.gogreen.api.services.ProposalService
import com.arkindustries.gogreen.database.AppDatabase
import com.arkindustries.gogreen.database.dao.AttachmentDao
import com.arkindustries.gogreen.database.dao.ProposalDao
import com.arkindustries.gogreen.databinding.FragmentReviewsBinding
import com.arkindustries.gogreen.ui.adapters.ReviewAdapter
import com.arkindustries.gogreen.ui.repositories.FileRepository
import com.arkindustries.gogreen.ui.repositories.ProposalRepository
import com.arkindustries.gogreen.ui.viewmodels.FileViewModel
import com.arkindustries.gogreen.ui.viewmodels.ProposalViewModel

class ReviewsFragment : Fragment() {
    private lateinit var reviewsBinding: FragmentReviewsBinding
    private lateinit var reviewAdapter: ReviewAdapter
    private lateinit var fileService: FileService
    private lateinit var attachmentDao: AttachmentDao
    private lateinit var fileRepository: FileRepository
    private lateinit var fileViewModel: FileViewModel
    private lateinit var proposalService: ProposalService
    private lateinit var proposalDao: ProposalDao
    private lateinit var proposalRepository: ProposalRepository
    private lateinit var proposalViewModel: ProposalViewModel
    private lateinit var appDatabase: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        reviewsBinding = FragmentReviewsBinding.inflate(inflater, container, false)
        return reviewsBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        appDatabase = AppDatabase.getInstance(requireContext())

        fileService = RetrofitClient.createFileService(requireContext())
        attachmentDao = appDatabase.attachmentDao()
        fileRepository = FileRepository(fileService, attachmentDao)
        fileViewModel = FileViewModel(fileRepository)

        proposalService = RetrofitClient.createProposalService(requireContext())
        proposalDao = appDatabase.proposalDao()
        proposalRepository = ProposalRepository(proposalDao, proposalService)
        proposalViewModel = ProposalViewModel(proposalRepository, fileRepository)

        parentFragmentManager.setFragmentResultListener("reviews", this) { _, bundle ->
            val userId = bundle.getString("userId")
            proposalViewModel.getProposalFeedbacks(userId!!)
            reviewsObserver()
        }
    }

    private fun reviewsObserver() {
        proposalViewModel.getProposalFeedbacks.observe(viewLifecycleOwner) {
            val reviews = it?.reviews

            if (reviews.isNullOrEmpty()) {
                reviewsBinding.noReviews.visibility = View.VISIBLE
                reviewsBinding.reviewsRv.visibility = View.INVISIBLE
            } else {
                reviewsBinding.noReviews.visibility = View.GONE
                reviewsBinding.reviewsRv.visibility = View.VISIBLE
                reviewAdapter.submitList(it.reviews)
            }
        }
    }
}