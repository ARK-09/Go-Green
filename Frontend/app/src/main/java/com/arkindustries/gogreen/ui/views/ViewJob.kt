package com.arkindustries.gogreen.ui.views

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.arkindustries.gogreen.databinding.ActivityViewJobBinding


class ViewJob : AppCompatActivity() {
    private lateinit var viewJobBinding: ActivityViewJobBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewJobBinding = ActivityViewJobBinding.inflate(layoutInflater)
        setContentView(viewJobBinding.root)

//        ArrayAdapter.createFromResource(
//            this,
//            R.array.project_timeline,
//            android.R.layout.simple_spinner_item
//        ).also { adapter ->
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//            viewJobBinding.deadlineSpinner.adapter = adapter
//        }
    }
}