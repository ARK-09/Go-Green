package com.arkindustries.gogreen.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.arkindustries.gogreen.api.response.Room
import com.arkindustries.gogreen.databinding.RoomListItemBinding
import com.bumptech.glide.Glide


class RoomAdapter(
    private val onItemClick: (Room) -> Unit,
    private val isUserClient: Boolean,
    private val loadMoreListener: (currentPage: Int) -> Unit,
    private val ITEMS_THRESHOLD_BEFORE_LOAD: Int = 5,
    private val pageSize: Int = 10
) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {
    private var isLoading = false
    private val rooms = mutableListOf<Room>()

    fun submitList(newRooms: List<Room>) {
        val diffCallback = RoomDiffCallback(rooms, newRooms)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        rooms.clear()
        rooms.addAll(newRooms)
        diffResult.dispatchUpdatesTo(this)
    }

    fun appendRooms(newRooms: List<Room>) {
        rooms.addAll(newRooms)
        notifyItemRangeInserted(rooms.size, newRooms.size)
    }

    fun addRoomAt(position: Int, newRoom: Room) {
        rooms.add(position, newRoom)
        notifyItemInserted(position)
        notifyItemMoved(position, position+1)
    }


    fun setLoading(isLoading: Boolean) {
        this.isLoading = isLoading
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RoomListItemBinding.inflate(inflater, parent, false)
        return RoomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val project = rooms[position]

        val threshold = if (rooms.size - ITEMS_THRESHOLD_BEFORE_LOAD < 0) {
            rooms.size
        } else {
            rooms.size - ITEMS_THRESHOLD_BEFORE_LOAD
        }

        if (position == threshold && isLoading) {
            val currentPage = rooms.size / pageSize
            loadMoreListener.invoke(if (currentPage < 1) 1 else currentPage )
        }
        holder.bind(project)
    }

    override fun getItemCount(): Int = rooms.size

    inner class RoomViewHolder(
        private val binding: RoomListItemBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(room: Room) {
            binding.apply {
                this.loading.visibility = if (isLoading) {
                    View.VISIBLE
                } else {
                    View.INVISIBLE
                }

                this.roomName.text = room.name
                this.roomLastMessage.text = room.lastMessage?.text?.ifBlank { "..." } ?: "..."
                if (isUserClient) {
                    Glide.with(binding.root).load(room.members[0].image.url)
                        .error(com.arkindustries.gogreen.R.drawable.test).into(binding.roomImage)
                } else {
                    Glide.with(binding.root).load(room.owner.image.url)
                        .error(com.arkindustries.gogreen.R.drawable.test)
                        .into(binding.roomImage)
                }
                this.root.setOnClickListener { onItemClick(room) }
            }
        }
    }
}

class RoomDiffCallback(
    private val oldList: List<Room>,
    private val newList: List<Room>
) :
    DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition]._id == newList[newItemPosition]._id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}

