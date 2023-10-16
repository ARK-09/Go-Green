package com.arkindustries.gogreen.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.arkindustries.gogreen.database.entites.AttachmentEntity
import com.arkindustries.gogreen.databinding.JobAttachmentListItemBinding

class ViewAttachmentsAdapter(
    var dataList: MutableList<AttachmentEntity> = mutableListOf(),
    private val onAttachmentClickListener: (position: Int, item: AttachmentEntity) -> Unit,
    private val onActionBtnClickListener: (position: Int, item: AttachmentEntity) -> Unit
) :
    RecyclerView.Adapter<ViewAttachmentsAdapter.FileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = JobAttachmentListItemBinding.inflate(inflater, parent, false)
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

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(dataList[position], onAttachmentClickListener, onActionBtnClickListener)
    }

    inner class FileViewHolder(private val binding: JobAttachmentListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: AttachmentEntity,
            onAttachmentClickListener: (position: Int, item: AttachmentEntity) -> Unit,
            onActionBtnClickListener: (position: Int, item: AttachmentEntity) -> Unit
        ) {
            val fileName = item.originalName
            binding.fileNameTv.text = fileName
            binding.fileNameTv.setOnClickListener {
                onAttachmentClickListener (bindingAdapterPosition, dataList[bindingAdapterPosition])
            }
            binding.actionBtn.setOnClickListener {
                onActionBtnClickListener(bindingAdapterPosition, dataList[bindingAdapterPosition])
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