package com.arkindustries.gogreen.ui.models

import androidx.databinding.BaseObservable
import androidx.databinding.ObservableField

data class LabelledItem<T> (var id: String, var title: String, var _isSelected: Boolean = false) : BaseObservable() {
    var isSelected = ObservableField(_isSelected)
}