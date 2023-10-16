package com.arkindustries.gogreen.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.arkindustries.gogreen.api.response.Project
import com.arkindustries.gogreen.databinding.ProjectListItemBinding

class ProjectsAdapter(
    private val onItemClick: (Project) -> Unit
) : RecyclerView.Adapter<ProjectsAdapter.ProjectViewHolder>() {

    private val projects = mutableListOf<Project>()

    fun submitList(newProjects: List<Project>) {
        val diffCallback = ProjectDiffCallback(projects, newProjects)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        projects.clear()
        projects.addAll(newProjects)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ProjectListItemBinding.inflate(inflater, parent, false)
        return ProjectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val project = projects[position]
        holder.bind(project)
    }

    override fun getItemCount(): Int = projects.size

    inner class ProjectViewHolder(
        private val binding: ProjectListItemBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(project: Project) {
            binding.apply {
                binding.image = project.attachments [bindingAdapterPosition].url
                binding.project = project
                binding.root.setOnClickListener { onItemClick(project) }
                binding.executePendingBindings()
            }
        }
    }
}

class ProjectDiffCallback(
    private val oldList: List<Project>,
    private val newList: List<Project>
) :
    DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition]._id == newList[newItemPosition]._id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}

