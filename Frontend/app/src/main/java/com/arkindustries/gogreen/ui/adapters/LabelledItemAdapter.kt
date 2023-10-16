package com.arkindustries.gogreen.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.arkindustries.gogreen.databinding.LabledListItemBinding
import com.arkindustries.gogreen.ui.models.LabelledItem

class LabelledItemAdapter<T>(
    private var dataList: List<LabelledItem<T>> = emptyList(),
    private val listener: OnItemClickListener<T>
) : RecyclerView.Adapter<LabelledItemAdapter<T>.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LabledListItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun updateData(newData: List<LabelledItem<T>>) {
        val diffCallback = DiffCallback(dataList, newData)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        dataList = newData
        diffResult.dispatchUpdatesTo(this)
    }

    inner class ViewHolder(private val binding: LabledListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    var item = dataList[position]
                    item.isSelected.set(!item.isSelected.get()!!)
                    listener.onItemClick(item)
                }
            }
        }

        fun bind(item: LabelledItem<T>) {
            binding.item = item
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

