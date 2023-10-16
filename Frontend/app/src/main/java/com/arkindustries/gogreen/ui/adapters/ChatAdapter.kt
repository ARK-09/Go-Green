package com.arkindustries.gogreen.ui.adapters

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.arkindustries.gogreen.R
import com.arkindustries.gogreen.api.response.RoomMessageUnpopulated
import com.arkindustries.gogreen.databinding.MessageReceivedBinding
import com.arkindustries.gogreen.databinding.MessageSentBinding
import com.arkindustries.gogreen.utils.DateTimeUtils.formatDateToTimeWithAmPm
import com.bumptech.glide.Glide

class ChatAdapter(
    private val context: Context,
    private val itemLongClickListener: (message: RoomMessageUnpopulated) -> Unit,
    private val currentUserId: String,
    private val loadMoreListener: (currentPage: Int) -> Unit,
    private val ITEMS_THRESHOLD_BEFORE_LOAD: Int = 5,
    private val pageSize: Int = 10
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var isLoading = false
    val messages = mutableListOf<RoomMessageUnpopulated>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == 1) {
            val binding = MessageSentBinding.inflate(inflater, parent, false)
            MessageSentViewHolder(binding)
        } else {
            val binding = MessageReceivedBinding.inflate(inflater, parent, false)
            MessageReceivedViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        val threshold = if (messages.size - ITEMS_THRESHOLD_BEFORE_LOAD < 0) {
            messages.size
        } else {
            messages.size - ITEMS_THRESHOLD_BEFORE_LOAD

        }

        if (position == threshold && isLoading) {
            val currentPage = messages.size / pageSize
            loadMoreListener.invoke(if (currentPage < 1) 1 else currentPage)
        }

        if (message.sender._id == currentUserId) {
            (holder as MessageSentViewHolder).bind(message)
        } else {
            (holder as MessageReceivedViewHolder).bind(message)
        }
    }

    override fun getItemCount(): Int = messages.size

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]
        return if (message.sender._id == currentUserId) {
            1 // means message MessageSentViewHolder
        } else {
            0
        }
    }

    fun setLoading(isLoading: Boolean) {
        this.isLoading = isLoading
    }

    fun submitList(newMessages: List<RoomMessageUnpopulated>) {
        val diffCallback = ChatDiffCallback(messages, newMessages)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        messages.clear()
        messages.addAll(newMessages)
        diffResult.dispatchUpdatesTo(this)
    }

    fun addAllItems(newMessages: List<RoomMessageUnpopulated>) {
        val startInsertPosition = messages.size
        messages.addAll(newMessages)
        notifyItemRangeInserted(startInsertPosition, newMessages.size)
    }

    inner class MessageSentViewHolder(private val binding: MessageSentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: RoomMessageUnpopulated) {
            binding.messageText.text = message.text

            if (message.attachments.isEmpty()) {
                val imageUrl = message.attachments.find { it.mimeType.contains("image", true) }
                if (!imageUrl?.url.isNullOrEmpty()) {
                    Glide.with(binding.root).load(imageUrl).error(R.drawable.test)
                        .into(binding.messageAttachment)
                }
            }

            binding.messageDate.text = formatDateToTimeWithAmPm(message.createdDate)
            binding.root.setOnLongClickListener {
                (context as Activity).registerForContextMenu(it)
                itemLongClickListener(message)
                return@setOnLongClickListener true
            }
        }
    }

    inner class MessageReceivedViewHolder(private val binding: MessageReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: RoomMessageUnpopulated) {
            binding.messageText.text = message.text

            if (message.attachments.isEmpty()) {
                val imageUrl = message.attachments.find { it.mimeType.contains("image", true) }
                if (!imageUrl?.url.isNullOrEmpty()) {
                    Glide.with(binding.root).load(imageUrl).error(R.drawable.test)
                        .into(binding.messageAttachment)
                }
            }

            binding.messageDate.text = formatDateToTimeWithAmPm(message.createdDate)
            binding.root.setOnLongClickListener {
                itemLongClickListener(message)
                return@setOnLongClickListener true
            }
        }
    }
}

class ChatDiffCallback(
    private val oldList: List<RoomMessageUnpopulated>,
    private val newList: List<RoomMessageUnpopulated>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition]._id == newList[newItemPosition]._id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].text == newList[newItemPosition].text
    }
}

