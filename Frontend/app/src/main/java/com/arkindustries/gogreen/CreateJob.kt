package com.arkindustries.gogreen

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.arkindustries.gogreen.databinding.ActivityCreateJobBinding
import com.arkindustries.gogreen.databinding.ActivityViewJobBinding


class CreateJob : AppCompatActivity() {
    private lateinit var createJobBinding: ActivityCreateJobBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createJobBinding = ActivityCreateJobBinding.inflate(layoutInflater)
        setContentView(createJobBinding.root)

//        ArrayAdapter.createFromResource(
//            this,
//            R.array.project_timeline,
//            android.R.layout.simple_spinner_item
//        ).also { adapter ->
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//            createJobBinding.deadlineSpinner.adapter = adapter
//        }
    }
}