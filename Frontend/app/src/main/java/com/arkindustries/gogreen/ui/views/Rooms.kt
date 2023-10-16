package com.arkindustries.gogreen.ui.views

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.transition.TransitionInflater
import com.arkindustries.gogreen.AppContext
import com.arkindustries.gogreen.R
import com.arkindustries.gogreen.api.RetrofitClient
import com.arkindustries.gogreen.api.request.CreateRoomRequest
import com.arkindustries.gogreen.api.response.Room
import com.arkindustries.gogreen.api.services.FileService
import com.arkindustries.gogreen.api.services.ProposalService
import com.arkindustries.gogreen.database.AppDatabase
import com.arkindustries.gogreen.database.dao.AttachmentDao
import com.arkindustries.gogreen.database.dao.ProposalDao
import com.arkindustries.gogreen.databinding.FragmentRoomsBinding
import com.arkindustries.gogreen.ui.adapters.RoomAdapter
import com.arkindustries.gogreen.ui.repositories.ChatRepository
import com.arkindustries.gogreen.ui.repositories.FileRepository
import com.arkindustries.gogreen.ui.repositories.ProposalRepository
import com.arkindustries.gogreen.ui.viewmodels.ChatViewModel
import com.arkindustries.gogreen.ui.viewmodels.FileViewModel
import com.arkindustries.gogreen.ui.viewmodels.ProposalViewModel
import com.arkindustries.gogreen.ui.viewmodels.factory.ChatViewModelFactory

class Rooms : Fragment() {
    private lateinit var roomsBinding: FragmentRoomsBinding
    private lateinit var roomAdapter: RoomAdapter
    private lateinit var chatRepository: ChatRepository
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var fileService: FileService
    private lateinit var attachmentDao: AttachmentDao
    private lateinit var fileRepository: FileRepository
    private lateinit var fileViewModel: FileViewModel
    private lateinit var proposalService: ProposalService
    private lateinit var proposalDao: ProposalDao
    private lateinit var proposalRepository: ProposalRepository
    private lateinit var proposalViewModel: ProposalViewModel
    private lateinit var appDatabase: AppDatabase
    private var isUserClient = false
    private var PAGE_SIZE = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isUserClient = AppContext.getInstance().currentUser.userType == "client"

        val onItemClickListener = { room: Room ->
            chatRoom(room._id)
        }

        val loadMoreListener = { currentPage: Int ->
            loadRooms(currentPage + 1, PAGE_SIZE)
        }

        roomAdapter = RoomAdapter(onItemClickListener, isUserClient, loadMoreListener, PAGE_SIZE)

        val chatService = RetrofitClient.createChatService(requireContext())
        chatRepository = ChatRepository(chatService)
        chatViewModel =
            ViewModelProvider(this, ChatViewModelFactory(chatRepository))[ChatViewModel::class.java]

        appDatabase = AppDatabase.getInstance(requireContext())

        fileService = RetrofitClient.createFileService(requireContext())
        attachmentDao = appDatabase.attachmentDao()
        fileRepository = FileRepository(fileService, attachmentDao)
        fileViewModel = FileViewModel(fileRepository)

        proposalService = RetrofitClient.createProposalService(requireContext())
        proposalDao = appDatabase.proposalDao()
        proposalRepository = ProposalRepository(proposalDao, proposalService)
        proposalViewModel = ProposalViewModel(proposalRepository, fileRepository)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        roomsBinding = FragmentRoomsBinding.inflate(inflater)
        enterTransition = TransitionInflater.from(requireContext()).inflateTransition(R.transition.slide_out)
        exitTransition = TransitionInflater.from(requireContext()).inflateTransition(R.transition.slide_in)
        return roomsBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setFragmentResultListener("createRoom") { _, bundle ->
            val proposalId = bundle.getString("proposalId")
            if (proposalId != null) {
                proposalViewModel.getProposalById(proposalId)
            }
        }

        roomsBinding.rooms.adapter = roomAdapter

        loadRooms(1, PAGE_SIZE)
        getRoomsObserver()
        createRoomObserver()
        proposalObserver ()
        proposalLoadingObserver()
        createRoomLoadingObserver()
    }

    private fun createRoomLoadingObserver() {
        chatViewModel.loading.observe(viewLifecycleOwner) {
            roomsBinding.progressBar.visibility = if (it) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun proposalLoadingObserver() {
        proposalViewModel.loadingState.observe(viewLifecycleOwner) {
            roomsBinding.progressBar.visibility = if (it) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun loadRooms(offset: Int, limit: Int) {
        roomAdapter.setLoading(true)
        chatViewModel.getRooms(offset, limit)
    }

    private fun proposalObserver() {
        proposalViewModel.getProposalById.observe(viewLifecycleOwner) {
            if (it != null) {
                val roomName = it.job.title
                val members = mutableListOf(it.proposal.user!!.userId)
                chatViewModel.createRoom(CreateRoomRequest(roomName, members))
            }
        }
    }

    private fun createRoomObserver() {
        chatViewModel.createRoomResponse.observe(viewLifecycleOwner) {
            if (it != null) {
                roomAdapter.addRoomAt(0, it.room)
                chatRoom(it.room._id)
            }
        }
    }

    private fun getRoomsObserver() {
        chatViewModel.getRoomsResponse.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it.rooms.isNotEmpty()) {
                    roomAdapter.submitList(it.rooms)
                    roomAdapter.setLoading(false)
                } else {
                    roomsBinding.noRooms.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun chatRoom (roomId: String) {
        val chatRoom = bundleOf("roomId" to roomId)
        val chatIntent = Intent(requireContext(), Chat::class.java)
        chatIntent.putExtra("chatRoom", chatRoom)
        startActivity(chatIntent)
    }
}