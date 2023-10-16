package com.arkindustries.gogreen.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.arkindustries.gogreen.api.response.Review
import com.arkindustries.gogreen.databinding.WorkHistoryListItemBinding

class ReviewAdapter(
    private val onItemClick: (Review) -> Unit
) : RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    private val reviews = mutableListOf<Review>()

    fun submitList(newReviews: List<Review>) {
        val diffCallback = ReviewDiffCallback(reviews, newReviews)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        reviews.clear()
        reviews.addAll(newReviews)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = WorkHistoryListItemBinding.inflate(inflater, parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val project = reviews[position]
        holder.bind(project)
    }

    override fun getItemCount(): Int = reviews.size

    inner class ReviewViewHolder(
        private val binding: WorkHistoryListItemBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(review: Review) {
            binding.apply {
                this.workTitle.text = review.doc.title
                this.workRatting.rating = review.clientRating.toFloat()
                this.startDate.text = review.doc.createdDate
                this.endDate.text = review.doc.createdDate
                this.feedback.text = review.clientFeedback
                this.root.setOnClickListener { onItemClick(review) }
            }
        }
    }
}

class ReviewDiffCallback(
    private val oldList: List<Review>,
    private val newList: List<Review>
) :
    DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].doc._id == newList[newItemPosition].doc._id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}

