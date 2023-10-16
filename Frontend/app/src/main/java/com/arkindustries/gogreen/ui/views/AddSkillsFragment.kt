package com.arkindustries.gogreen.ui.views

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.arkindustries.gogreen.api.RetrofitClient
import com.arkindustries.gogreen.api.response.Skill
import com.arkindustries.gogreen.api.services.SkillService
import com.arkindustries.gogreen.database.AppDatabase
import com.arkindustries.gogreen.database.entites.SkillEntity
import com.arkindustries.gogreen.databinding.FragmentAddSkillsBinding
import com.arkindustries.gogreen.ui.adapters.LabelledItemAdapter
import com.arkindustries.gogreen.ui.models.LabelledItem
import com.arkindustries.gogreen.ui.repositories.SkillRepository
import com.arkindustries.gogreen.ui.viewmodels.SkillViewModel
import com.arkindustries.gogreen.ui.viewmodels.factory.SkillViewModelFactory
import com.google.android.material.snackbar.Snackbar

class AddSkillsFragment : DialogFragment() {
    private lateinit var addSkillsBinding: FragmentAddSkillsBinding
    private lateinit var appDatabase: AppDatabase
    private lateinit var skillService: SkillService
    private lateinit var skillRepository: SkillRepository
    private lateinit var skillsViewModel: SkillViewModel
    private lateinit var skillsAdapter: LabelledItemAdapter<SkillEntity>
    private var skills = mutableListOf<Skill>()

    interface OnSkillsSelectListener {
        fun onSkillsSelect(skills: List<Skill>)
    }

    private lateinit var skillsSelectListener: OnSkillsSelectListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            skillsSelectListener = context as OnSkillsSelectListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement OnSkillsSelectListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        addSkillsBinding = FragmentAddSkillsBinding.inflate(layoutInflater, container, false)
        return addSkillsBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appDatabase = AppDatabase.getInstance(requireContext())
        skillService = RetrofitClient.createSkillService(requireContext())
        skillRepository = SkillRepository(skillService, appDatabase.skillDao())

        skillsViewModel = ViewModelProvider(
            this,
            SkillViewModelFactory(skillRepository)
        )[SkillViewModel::class.java]

        val skillItemClickListener =
            object : LabelledItemAdapter.OnItemClickListener<SkillEntity> {
                override fun onItemClick(item: LabelledItem<SkillEntity>) {
                    val isSelected = item.isSelected.get() ?: false

                    if (isSelected) {
                        skills.add(Skill(item.id, item.title))
                    } else {
                        skills.remove(Skill(item.id, item.title))
                    }
                }

            }

        addSkillsBinding.done.setOnClickListener {
            if (skills.isNotEmpty()) {
                dismiss()
                skillsSelectListener.onSkillsSelect(skills)
            } else {
                Toast.makeText(requireContext(), "Please select at least on skill", Toast.LENGTH_SHORT).show()
            }
        }

        addSkillsBinding.cancel.setOnClickListener {
            dismiss()
        }

        skillsAdapter = LabelledItemAdapter(listener = skillItemClickListener)
        addSkillsBinding.skillsRv.adapter = skillsAdapter
        skillsViewModel.refreshSkills()

        skillsObserver ()
        skillsErrorObserver ()
    }

    private fun skillsObserver () {
        skillsViewModel.skills.observe(viewLifecycleOwner) {
            val labelledItems = it.map { skillEntity ->
                return@map LabelledItem<SkillEntity> (skillEntity.skillId, skillEntity.title)
            }
            skillsAdapter.updateData(labelledItems)
        }
    }

    private fun skillsErrorObserver () {
        skillsViewModel.error.observe(viewLifecycleOwner) {
            Snackbar.make(addSkillsBinding.root, it.message!!, Snackbar.LENGTH_SHORT).show()
            childFragmentManager.popBackStack()
        }
    }
}