package com.arkindustries.gogreen.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.arkindustries.gogreen.R
import com.arkindustries.gogreen.api.response.GeocodingSearchResponse


class LocationAdapter(
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<LocationAdapter.ViewHolder>() {
    var dataList = mutableListOf<GeocodingSearchResponse>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.location_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        dataList[position].let { holder.bind(it) }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun updateData(newData: List<GeocodingSearchResponse>) {
        val diffCallback = LocationDiffCallback(dataList, newData)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        dataList.clear()
        dataList.addAll(newData)
        diffResult.dispatchUpdatesTo(this)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.location_name)

        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = dataList[position]
                    listener.onItemClick(item)
                }
            }
        }

        fun bind(item: GeocodingSearchResponse) {
            textView.text = item.display_name
        }
    }

    interface OnItemClickListener {
        fun onItemClick(item: GeocodingSearchResponse)
    }

}

class LocationDiffCallback(
    private val oldList: List<GeocodingSearchResponse>,
    private val newList: List<GeocodingSearchResponse>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].place_id == newList[newItemPosition].place_id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].display_name == newList[newItemPosition].display_name
    }
}

