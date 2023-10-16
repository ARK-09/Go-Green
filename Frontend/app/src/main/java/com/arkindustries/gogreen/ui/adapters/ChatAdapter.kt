package com.arkindustries.gogreen.ui.adapters

import android.view.LayoutInflater
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.arkindustries.gogreen.databinding.LabledListItemBinding
import com.arkindustries.gogreen.ui.models.LabelledItem

class ChatAdapter(
    private val itemLongClickListener: OnLongClickListener,
) : RecyclerView.Adapter<ChatAdapter.LabelledItemViewHolder>() {
    private var dataList = mutableListOf<LabelledItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LabelledItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LabledListItemBinding.inflate(inflater, parent, false)
        return LabelledItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LabelledItemViewHolder, position: Int) {
        val item = dataList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun updateData(newData: List<LabelledItem>) {
        val diffCallback = DiffCallback(dataList, newData)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        dataList.clear()
        dataList.addAll(newData)
        diffResult.dispatchUpdatesTo(this)
    }

    inner class LabelledItemViewHolder(private val binding: LabledListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = dataList[position]
                    item.isSelected.set(!item.isSelected.get()!!)
                    listener.onItemClick(item)
                }
            }
        }

        fun bind(item: LabelledItem) {
            binding.item = item
            binding.executePendingBindings()
        }
    }

    interface OnItemClickListener {
        fun onItemClick(item: LabelledItem)
    }
}

class DiffCallback(
    private val oldList: List<LabelledItem>,
    private val newList: List<LabelledItem>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].title == newList[newItemPosition].title
    }
}

