package com.arkindustries.gogreen.ui.views

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.arkindustries.gogreen.AppContext
import com.arkindustries.gogreen.api.RetrofitClient
import com.arkindustries.gogreen.api.request.ReviewRequest
import com.arkindustries.gogreen.api.services.FileService
import com.arkindustries.gogreen.api.services.ProposalService
import com.arkindustries.gogreen.database.AppDatabase
import com.arkindustries.gogreen.database.dao.AttachmentDao
import com.arkindustries.gogreen.database.dao.ProposalDao
import com.arkindustries.gogreen.databinding.FragmentFeedbackBinding
import com.arkindustries.gogreen.ui.repositories.FileRepository
import com.arkindustries.gogreen.ui.repositories.ProposalRepository
import com.arkindustries.gogreen.ui.viewmodels.ProposalViewModel
import com.arkindustries.gogreen.ui.viewmodels.factory.ProposalViewModelFactory

class FeedbackFragment : DialogFragment() {
    private lateinit var fragmentFeedbackBinding: FragmentFeedbackBinding
    private lateinit var fileService: FileService
    private lateinit var attachmentDao: AttachmentDao
    private lateinit var fileRepository: FileRepository
    private lateinit var proposalService: ProposalService
    private lateinit var proposalDao: ProposalDao
    private lateinit var proposalRepository: ProposalRepository
    private lateinit var proposalViewModel: ProposalViewModel
    private lateinit var appDatabase: AppDatabase
    private var isUserClient: Boolean = false

    interface OnFeedbackListener {
        fun onFeedBack(feedback: ReviewRequest)
    }

    private lateinit var onFeedbackListener: OnFeedbackListener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            onFeedbackListener = context as OnFeedbackListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement onFeedbackListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isUserClient = AppContext.getInstance().currentUser.userType == "client"

        appDatabase = AppDatabase.getInstance(requireContext())
        fileService = RetrofitClient.createFileService(requireContext())
        attachmentDao = appDatabase.attachmentDao()
        fileRepository = FileRepository(fileService, attachmentDao)
        proposalDao = appDatabase.proposalDao()
        proposalRepository = ProposalRepository(proposalDao, proposalService)
        proposalViewModel =
            ViewModelProvider(
                this,
                ProposalViewModelFactory(
                    proposalRepository,
                    fileRepository
                )
            )[ProposalViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentFeedbackBinding = FragmentFeedbackBinding.inflate(layoutInflater, container, false)
        return fragmentFeedbackBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!isUserClient) {
            fragmentFeedbackBinding.feedbackLabel.text.replace(Regex("talent service"), "client")
        }

        fragmentFeedbackBinding.ratingBar.setOnRatingBarChangeListener { _: RatingBar, rating: Float, _: Boolean ->
            fragmentFeedbackBinding.ratingTv.text = rating.toString()
        }

        fragmentFeedbackBinding.done.setOnClickListener {
            val rating = fragmentFeedbackBinding.ratingBar.rating
            val feedback = fragmentFeedbackBinding.feedbackTi.editText?.text.toString()
            onFeedbackListener.onFeedBack(ReviewRequest(feedback, rating.toDouble()))
        }

        fragmentFeedbackBinding.cancel.setOnClickListener {
            dismiss()
        }
    }
}