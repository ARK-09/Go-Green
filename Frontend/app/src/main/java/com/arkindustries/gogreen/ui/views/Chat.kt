package com.arkindustries.gogreen.ui.views

import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.arkindustries.gogreen.AppContext
import com.arkindustries.gogreen.R
import com.arkindustries.gogreen.api.RetrofitClient
import com.arkindustries.gogreen.api.response.RoomMessageUnpopulated
import com.arkindustries.gogreen.api.services.ChatService
import com.arkindustries.gogreen.api.services.FileService
import com.arkindustries.gogreen.api.services.ProposalService
import com.arkindustries.gogreen.database.AppDatabase
import com.arkindustries.gogreen.database.dao.AttachmentDao
import com.arkindustries.gogreen.database.dao.ProposalDao
import com.arkindustries.gogreen.databinding.ActivityChatBinding
import com.arkindustries.gogreen.socket.request.MessageRequest
import com.arkindustries.gogreen.socket.request.RoomJoin
import com.arkindustries.gogreen.socket.response.SuccessResponse
import com.arkindustries.gogreen.ui.adapters.ChatAdapter
import com.arkindustries.gogreen.ui.repositories.ChatRepository
import com.arkindustries.gogreen.ui.repositories.FileRepository
import com.arkindustries.gogreen.ui.repositories.ProposalRepository
import com.arkindustries.gogreen.ui.viewmodels.ChatViewModel
import com.arkindustries.gogreen.ui.viewmodels.FileViewModel
import com.arkindustries.gogreen.ui.viewmodels.ProposalViewModel
import com.arkindustries.gogreen.ui.viewmodels.factory.ChatViewModelFactory
import com.arkindustries.gogreen.utils.JSONUtil
import com.bumptech.glide.Glide
import io.socket.client.IO
import io.socket.client.Manager
import io.socket.client.Socket
import io.socket.engineio.client.Transport
import java.net.URISyntaxException
import java.text.SimpleDateFormat
import java.util.Locale


class Chat : AppCompatActivity() {
    private lateinit var chatBinding: ActivityChatBinding
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatService: ChatService
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
    private lateinit var roomId: String
    private val baseUrl = "https://absolutely-sharp-llama.ngrok-free.app/"
    private val chatPath = "/api/v1/chats/socket.io"
    private var mSocket: Socket? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val options = IO.Options()
            options.path = chatPath
            mSocket = IO.socket(baseUrl, options)
        } catch (e: URISyntaxException) {
            Log.e(Socket::class.java.simpleName, e.printStackTrace().toString())
        }

        if (mSocket == null) {
            Toast.makeText(this, "Failed to connect to server. Please Try again", Toast.LENGTH_LONG)
                .show()
            finish()
        }

        mSocket!!.io().on(Manager.EVENT_TRANSPORT) { args ->
            val transport = args[0] as Transport
            transport.on(Transport.EVENT_REQUEST_HEADERS) { requestHeaders ->
                val headers = requestHeaders[0] as MutableMap<String, List<String>>
                headers["Authorization"] =
                    listOf("Bearer ${AppContext.getInstance().userSessionManager.getJwtToken(this)}")
            }
        }

        mSocket!!.connect()

        chatBinding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(chatBinding.root)
        chatBinding.send.isEnabled = false

        isUserClient = AppContext.getInstance().currentUser.userType == "client"


        chatBinding.backBtn.setOnClickListener {
            finish()
        }

        val onMessageLongClick = { message: RoomMessageUnpopulated ->

        }

        val loadMoreListener = { currentPage: Int ->
            Toast.makeText(this, "LoadMore", Toast.LENGTH_SHORT).show()
            loadMoreMessages(currentPage + 1, PAGE_SIZE)
        }

        chatAdapter = ChatAdapter(
            this,
            onMessageLongClick,
            AppContext.getInstance().currentUser.userId,
            loadMoreListener,
            5,
            PAGE_SIZE
        )
        chatService = RetrofitClient.createChatService(this)
        chatRepository = ChatRepository(chatService)
        chatViewModel =
            ViewModelProvider(this, ChatViewModelFactory(chatRepository))[ChatViewModel::class.java]
        chatBinding.messages.adapter = chatAdapter
        (chatBinding.messages.layoutManager as LinearLayoutManager).stackFromEnd = true
        (chatBinding.messages.layoutManager as LinearLayoutManager).reverseLayout = true

        appDatabase = AppDatabase.getInstance(this)

        fileService = RetrofitClient.createFileService(this)
        attachmentDao = appDatabase.attachmentDao()
        fileRepository = FileRepository(fileService, attachmentDao)
        fileViewModel = FileViewModel(fileRepository)

        proposalService = RetrofitClient.createProposalService(this)
        proposalDao = appDatabase.proposalDao()
        proposalRepository = ProposalRepository(proposalDao, proposalService)
        proposalViewModel = ProposalViewModel(proposalRepository, fileRepository)

        roomId = intent.getBundleExtra("chatRoom")?.getString("roomId") ?: ""

        if (roomId.isNotEmpty()) {
            val data = JSONUtil.toJson(RoomJoin(roomId), RoomJoin::class.java)
            mSocket!!.emit("room:join", data)
        }

        chatBinding.send.setOnClickListener {
            mSocket!!.emit("message:send", JSONUtil.toJson(MessageRequest(roomId, chatBinding.messageEt.text.toString(), null), MessageRequest::class.java))
        }

        mSocket!!.on("message:sent") { data ->
            runOnUiThread {
                val message = JSONUtil.fromJson(data[0].toString(), RoomMessageUnpopulated::class.java)
                if (message != null) {
                    chatAdapter.addAllItems(mutableListOf(message))
                    chatBinding.messageEt.text.clear()
                    chatBinding.messages.scrollToPosition(chatAdapter.itemCount - 1)
                }
            }
        }

        mSocket!!.on("room:joined") { data ->
            runOnUiThread {
                chatBinding.send.isEnabled = true
                Toast.makeText(
                    this,
                    JSONUtil.fromJson(data[0].toString(), SuccessResponse::class.java)?.message,
                    Toast.LENGTH_SHORT
                ).show()
                chatViewModel.getRoomById(roomId)
            }
        }

        mSocket!!.on("message:receive") {
            runOnUiThread {
                if (it != null) {
                    val message = JSONUtil.fromJson(it[0].toString(), RoomMessageUnpopulated::class.java)
                    if (message != null) {
                        chatAdapter.addAllItems(mutableListOf(message))
                        chatBinding.messages.scrollToPosition(chatAdapter.itemCount - 1)
                    }
                }
            }
        }

        mSocket!!.on("error") {
            runOnUiThread {
                Log.e(Socket::class.simpleName, it[0].toString())
                Toast.makeText(this, "Error: connecting" + it[0].toString(), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        roomObserver()
        roomErrorObserver ()
        messagesObserver()
    }

    override fun onCreateContextMenu(
        menu: ContextMenu, v: View,
        menuInfo: ContextMenu.ContextMenuInfo
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.message_context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        return when (item.itemId) {
            R.id.edit -> {
                Toast.makeText(this, "Edit Clicked" + info.position, Toast.LENGTH_SHORT).show()
                true
            }

            R.id.delete -> {
                Toast.makeText(this, "Edit Clicked" + info.position, Toast.LENGTH_SHORT).show()
                true
            }

            R.id.forward -> {
                Toast.makeText(this, "Forward Clicked" + info.position, Toast.LENGTH_SHORT).show()
                true
            }

            else -> super.onContextItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mSocket!!.disconnect()
    }

    private fun loadMoreMessages(offset: Int, limit: Int) {
        chatAdapter.setLoading(true)
        chatViewModel.getRoomMessages(roomId, offset, limit)
    }

    private fun messagesObserver() {
        chatViewModel.getRoomMessagesResponse.observe(this) { messagesResponse ->
            if (messagesResponse != null) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

                val messages =
                    messagesResponse.messages.sortedBy { dateFormat.parse(it.createdDate) }
                chatAdapter.addAllItems(messages)
                chatBinding.messages.scrollToPosition(chatAdapter.itemCount - 1)
            }

        }
    }

    private fun roomObserver() {
        chatViewModel.getRoomByIdResponse.observe(this) {
            if (it != null) {
                chatBinding.roomName.text = it.room.name
                chatBinding.userStatus.text = it.room.members[(Math.random() * it.room.members.size).toInt()].userStatus
                Glide.with(chatBinding.root).load(it.room.members[(Math.random() * it.room.members.size).toInt()].image.url)
                    .error(R.drawable.test)
                    .into(chatBinding.roomImage)
                chatViewModel.getRoomMessages(roomId, 1, PAGE_SIZE)
            }
        }
    }

    private fun roomErrorObserver () {
        chatViewModel.error.observe(this) {
            if (it != null) {
                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}