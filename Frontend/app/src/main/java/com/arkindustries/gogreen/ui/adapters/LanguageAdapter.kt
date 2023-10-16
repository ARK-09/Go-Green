package com.arkindustries.gogreen.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.arkindustries.gogreen.api.response.Language
import com.arkindustries.gogreen.databinding.LanguageListItemBinding

class LanguageAdapter(
    private val onItemClick: (Language) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

    private val languages = mutableListOf<Language>()

    fun submitList(newLanguages: List<Language>) {
        val diffCallback = LanguageDiffCallback(languages, newLanguages)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        languages.clear()
        languages.addAll(newLanguages)
        diffResult.dispatchUpdatesTo(this)
    }

    fun appendList(newLanguages: List<Language>) {
        val oldSize = languages.size
        languages.addAll(newLanguages)
        notifyItemRangeInserted(oldSize, newLanguages.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LanguageListItemBinding.inflate(inflater, parent, false)
        return LanguageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val project = languages[position]
        holder.bind(project)
    }

    override fun getItemCount(): Int = languages.size

    inner class LanguageViewHolder(
        private val binding: LanguageListItemBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(language: Language) {
            binding.apply {
                binding.languageTitle.text = language.name
                binding.experience.text = language.experience
                binding.root.setOnClickListener { onItemClick(language) }
            }
        }
    }
}

class LanguageDiffCallback(
    private val oldList: List<Language>,
    private val newList: List<Language>
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

