package com.arkindustries.gogreen.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.arkindustries.gogreen.database.entites.AttachmentEntity
import com.arkindustries.gogreen.databinding.AttachmentListItemBinding

class UploadAttachmentsAdapter(
    var dataList: MutableList<AttachmentEntity> = mutableListOf(),
    private val progress: Int,
    private val onDeleteClickListener: (position: Int, item: AttachmentEntity) -> Unit
) :
    RecyclerView.Adapter<UploadAttachmentsAdapter.FileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = AttachmentListItemBinding.inflate(layoutInflater, parent, false)
        return FileViewHolder(binding)
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

    fun removeItem(condition: (attachmentEntity: AttachmentEntity) -> Boolean) {
        val iterator = dataList.listIterator(dataList.size)

        while (iterator.hasPrevious()) {
            val position = iterator.previousIndex()
            val item = iterator.previous()

            if (condition(item)) {
                iterator.remove()
                notifyItemRemoved(position)
            }
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(dataList[position], onDeleteClickListener)
    }

    inner class FileViewHolder(val binding: AttachmentListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: AttachmentEntity,
            onDeleteClickListener: (position: Int, item: AttachmentEntity) -> Unit
        ) {
            val fileName = item.originalName
            binding.fileNameTv.text = fileName
            binding.fileProgress.progress = progress
            binding.actionBtn.setOnClickListener {
                onDeleteClickListener(bindingAdapterPosition, dataList[bindingAdapterPosition])
            }
            binding.actionBtn.isEnabled = item.url == null
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