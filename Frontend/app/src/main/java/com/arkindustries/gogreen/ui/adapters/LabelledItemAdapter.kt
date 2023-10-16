package com.arkindustries.gogreen.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.arkindustries.gogreen.databinding.LabledListItemBinding
import com.arkindustries.gogreen.ui.models.LabelledItem

class LabelledItemAdapter<T>(
    private val listener: OnItemClickListener<T>
) : RecyclerView.Adapter<LabelledItemAdapter<T>.LabelledItemViewHolder>() {
    private var dataList = mutableListOf<LabelledItem<T>>()

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

    fun updateData(newData: List<LabelledItem<T>>) {
        val diffCallback = DiffCallback(dataList, newData)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        dataList.clear()
        dataList.addAll(newData)
        diffResult.dispatchUpdatesTo(this)
    }

    fun appendList(newData: List<LabelledItem<T>>) {
        val oldSize = dataList.size
        dataList.addAll(newData)
        notifyItemRangeInserted(oldSize, newData.size)
    }

    inner class LabelledItemViewHolder(private val binding: LabledListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LabelledItem<T>) {
            binding.item = item
            binding.root.setOnClickListener {
                item.isSelected.set(!item.isSelected.get()!!)
                listener.onItemClick(item)
            }
            binding.executePendingBindings()
        }
    }

    interface OnItemClickListener<T> {
        fun onItemClick(item: LabelledItem<T>)
    }
}

class DiffCallback<T>(
    private val oldList: List<LabelledItem<T>>,
    private val newList: List<LabelledItem<T>>
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

