package com.arkindustries.gogreen.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.arkindustries.gogreen.R
import com.arkindustries.gogreen.database.entites.AttachmentEntity

class UploadAttachmentsAdapter(
    var dataList: MutableList<AttachmentEntity> = mutableListOf(),
    private val progress: Int,
    private val onDeleteClickListener: (position: Int, item: AttachmentEntity) -> Unit
) :
    RecyclerView.Adapter<UploadAttachmentsAdapter.FileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.attachment_list_item, parent, false)
        return FileViewHolder(view)
    }

    fun updateData(newData: List<AttachmentEntity>) {
        val diffCallback = DiffCallback(dataList, newData)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        dataList.clear()
        dataList.addAll(newData)
        diffResult.dispatchUpdatesTo(this)
    }

    fun addAllItems(newItems: List<AttachmentEntity>) {
        val startInsertPosition = dataList.size
        dataList.addAll(newItems)
        notifyItemRangeInserted(startInsertPosition, newItems.size)
    }

    fun removeItem(position: Int) {
        dataList.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(dataList[position], onDeleteClickListener)
    }

    inner class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val fileNameTextView: TextView = itemView.findViewById(R.id.file_name_tv)
        var progressBar: ProgressBar = itemView.findViewById(R.id.file_progress)
        private val deleteImageView: ImageView = itemView.findViewById(R.id.action_btn)

        fun bind(item: AttachmentEntity, onDeleteClickListener: (position: Int, item: AttachmentEntity) -> Unit) {
            val fileName = item.originalName
            fileNameTextView.text = fileName
            progressBar.progress = progress
            deleteImageView.setOnClickListener {
                onDeleteClickListener(bindingAdapterPosition, dataList[bindingAdapterPosition])
            }
        }
    }

    private inner class DiffCallback(
        private val oldList: List<AttachmentEntity>,
        private val newList: List<AttachmentEntity>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].attachmentId == newList[newItemPosition].attachmentId
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].originalName == newList[newItemPosition].originalName
        }
    }
}