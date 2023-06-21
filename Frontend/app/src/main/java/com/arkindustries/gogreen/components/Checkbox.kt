package com.arkindustries.gogreen.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.arkindustries.gogreen.R

class Checkbox @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val checkboxIcon: ImageView
    private val checkboxLabel: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.check_box, this, true)
        checkboxIcon = findViewById(R.id.checkbox_icon)
        checkboxLabel = findViewById(R.id.checkbox_label)

        orientation = HORIZONTAL
        attrs?.let { retrieveAttributes(it) }
    }

    private fun retrieveAttributes(attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.Checkbox)

        val labelText = typedArray.getString(R.styleable.Checkbox_labelText)
        val isChecked = typedArray.getBoolean(R.styleable.Checkbox_checked, false)

        checkboxLabel.text = labelText
        checkboxIcon.setImageResource(if (isChecked) R.drawable.ic_checked else R.drawable.ic_unchecked)

        typedArray.recycle()
    }

    fun setChecked(checked: Boolean) {
        checkboxIcon.setImageResource(if (checked) R.drawable.ic_checked else R.drawable.ic_unchecked)
    }

    fun isChecked(): Boolean {
        return checkboxIcon.drawable.constantState == ResourcesCompat.getDrawable(resources, R.drawable.ic_checked, null)?.constantState
    }
}
