package com.arkindustries.gogreen.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.arkindustries.gogreen.database.entites.JobEntity
import com.arkindustries.gogreen.databinding.JobListItemBinding

class JobAdapter(
    private val onItemClick: (JobEntity) -> Unit,
    private val onOfferClick: (JobEntity) -> Unit,
    var isUserClient: Boolean = false
) : RecyclerView.Adapter<JobAdapter.JobViewHolder>() {

    private val jobs = mutableListOf<JobEntity>()

    fun submitList(newJobs: List<JobEntity>) {
        val diffCallback = JobDiffCallback(jobs, newJobs)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        jobs.clear()
        jobs.addAll(newJobs)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = JobListItemBinding.inflate(inflater, parent, false)
        binding.sendOfferBtn.visibility = if (isUserClient) {
            View.INVISIBLE
        } else {
            View.VISIBLE
        }
        return JobViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        val job = jobs[position]
        holder.bind(job)
    }

    override fun getItemCount(): Int = jobs.size

    inner class JobViewHolder(
        private val binding: JobListItemBinding
    ) :
        RecyclerView.ViewHolder(binding.root), LifecycleOwner {
        private val lifecycleRegistry = LifecycleRegistry(this)

        init {
            lifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
        }

        fun onAppear() {
            lifecycleRegistry.currentState = Lifecycle.State.CREATED
            lifecycleRegistry.currentState = Lifecycle.State.STARTED
        }

        fun onDisappear() {
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        }

        fun bind(job: JobEntity) {
            binding.apply {
                lifecycleOwner = this@JobViewHolder
                binding.job = job
                binding.user = job.user

                binding.root.setOnClickListener { onItemClick(job) }
                binding.sendOfferBtn.setOnClickListener { onOfferClick(job) }

                binding.executePendingBindings()
            }
        }

        override val lifecycle: Lifecycle
            get() = lifecycleRegistry
    }
}

class JobDiffCallback(
    private val oldList: List<JobEntity>,
    private val newList: List<JobEntity>
) :
    DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].jobId == newList[newItemPosition].jobId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}

