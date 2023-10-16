package com.arkindustries.gogreen.ui.views

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.arkindustries.gogreen.api.response.Language
import com.arkindustries.gogreen.databinding.FragmentAddLanguagesBinding

class AddLanguagesFragment : DialogFragment() {
    private lateinit var addLanguagesBinding: FragmentAddLanguagesBinding

    interface OnLanguageSelectListener {
        fun onLanguageSelect(language: Language)
    }

    private lateinit var onLanguageSelect: OnLanguageSelectListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            onLanguageSelect = context as OnLanguageSelectListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement OnLanguageSelectListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        addLanguagesBinding = FragmentAddLanguagesBinding.inflate(layoutInflater, container, false)
        return addLanguagesBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addLanguagesBinding.done.setOnClickListener {
            onLanguageSelect.onLanguageSelect(
                Language(
                    null,
                    addLanguagesBinding.languagesSpinner.selectedItem as String,
                    addLanguagesBinding.languagesExperienceSpinner.selectedItem as String
                )
            )
            dismiss()
        }

        addLanguagesBinding.cancel.setOnClickListener {
            dismiss()
        }
    }
}