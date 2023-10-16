package com.arkindustries.gogreen.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.arkindustries.gogreen.R
import com.arkindustries.gogreen.database.entites.ProposalWithAttachmentsAndJob
import com.arkindustries.gogreen.databinding.ProposalListItemBinding
import com.arkindustries.gogreen.ui.bindingadapters.location
import com.bumptech.glide.Glide

class ProposalsAdapter(
    private val onItemClick: (ProposalWithAttachmentsAndJob) -> Unit,
    private val onOfferClick: (ProposalWithAttachmentsAndJob) -> Unit,
    var isUserClient: Boolean = false
) : RecyclerView.Adapter<ProposalsAdapter.ProposalsViewHolder>() {
    val proposalWithAttachmentsAndJob = mutableListOf<ProposalWithAttachmentsAndJob>()


    fun submitList(newProposals: List<ProposalWithAttachmentsAndJob>) {
        val diffCallback = ProposalDiffCallback(proposalWithAttachmentsAndJob, newProposals)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        proposalWithAttachmentsAndJob.clear()
        proposalWithAttachmentsAndJob.addAll(newProposals)
        diffResult.dispatchUpdatesTo(this)
    }
    
    fun appendList (newProposals: List<ProposalWithAttachmentsAndJob>) {
        val diffCallback = ProposalDiffCallback(proposalWithAttachmentsAndJob, newProposals)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        proposalWithAttachmentsAndJob.addAll(newProposals)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProposalsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ProposalListItemBinding.inflate(inflater, parent, false)
        return ProposalsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProposalsViewHolder, position: Int) {
        val proposal = proposalWithAttachmentsAndJob[position]
        holder.bind(proposal)
    }

    override fun getItemCount(): Int = proposalWithAttachmentsAndJob.size

    inner class ProposalsViewHolder(
        private val binding: ProposalListItemBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(proposal: ProposalWithAttachmentsAndJob) {
            binding.apply {
                this.root.setOnClickListener { onItemClick(proposal) }

                if (isUserClient) {
                    this.sendOfferBtn.setOnClickListener { onOfferClick(proposal) }
                }
            }

            if (isUserClient) {
                populateProposalForClient(proposal)
            } else {
                populateProposalForTalent(proposal)
            }
        }

        private fun loadUserImage (proposal: ProposalWithAttachmentsAndJob) {
            if (isUserClient) {
                Glide.with(binding.root).load(proposal.proposal.user?.image?.url).error(R.drawable.test).into(binding.userIv)
            } else {
                Glide.with(binding.root).load(proposal.job.user?.image?.url).error(R.drawable.test).into(binding.userIv)
            }
        }

        private fun populateProposalForClient (proposal: ProposalWithAttachmentsAndJob) {
            loadUserImage(proposal)
            binding.jobTitleTv.text = proposal.job.title
            binding.userNameTv.text = proposal.proposal.user!!.name
            binding.jobDescriptionTv.text = proposal.proposal.coverLetter
            binding.locationContainer.visibility = View.INVISIBLE
            binding.priceTv.text = proposal.proposal.bidAmount.toString()
        }

        private fun populateProposalForTalent (proposal: ProposalWithAttachmentsAndJob) {
            loadUserImage(proposal)
            binding.jobTitleTv.text = proposal.job.title
            binding.userNameTv.text = proposal.job.user!!.name
            binding.jobDescriptionTv.text = proposal.job.description
            binding.locationTv.location(proposal.job.location)
            binding.priceTv.text = proposal.job.budget.toString()
            binding.sendOfferBtn.visibility = View.INVISIBLE
        }
    }
}

class ProposalDiffCallback(
    private val oldList: List<ProposalWithAttachmentsAndJob>,
    private val newList: List<ProposalWithAttachmentsAndJob>
) :
    DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].proposal.proposalId == newList[newItemPosition].proposal.proposalId
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}


