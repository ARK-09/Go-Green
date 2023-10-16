package com.arkindustries.gogreen.ui.views

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.arkindustries.gogreen.api.RetrofitClient
import com.arkindustries.gogreen.api.services.FileService
import com.arkindustries.gogreen.database.AppDatabase
import com.arkindustries.gogreen.databinding.ActivityFileViewerBinding
import com.arkindustries.gogreen.ui.repositories.FileRepository
import com.arkindustries.gogreen.ui.viewmodels.FileViewModel
import com.arkindustries.gogreen.ui.viewmodels.factory.FileViewModelFactory
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar

class FileViewer : AppCompatActivity() {
    private lateinit var fileViewBinding: ActivityFileViewerBinding
    private lateinit var fileService: FileService
    private lateinit var fileRepository: FileRepository
    private lateinit var fileViewModel: FileViewModel
    private lateinit var appDatabase: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fileViewBinding = ActivityFileViewerBinding.inflate(layoutInflater)
        setContentView(fileViewBinding.root)

        val fileId = intent.getStringExtra("fileId")

        if (fileId.isNullOrEmpty()) {
            Snackbar.make(fileViewBinding.root, "Invalid file id", Snackbar.LENGTH_SHORT).show()
            finish()
        }

        fileViewBinding.backBtn.setOnClickListener {
            finish()
        }

        appDatabase = AppDatabase.getInstance(this)
        fileService = RetrofitClient.createFileService(this)
        fileRepository = FileRepository(fileService, appDatabase.attachmentDao())
        fileViewModel = ViewModelProvider(this, FileViewModelFactory(fileRepository))[FileViewModel::class.java]

        fileViewModel.getFile(fileId!!)

        getFileObserver ()
        getFileErrorObserver()
    }

    private fun getFileObserver () {
        fileViewModel.getFileResult.observe(this) {
            if (it.mimeType.contains("image")) {
                Glide.with(this).load(it.url).into(fileViewBinding.imageIv)
                fileViewBinding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun getFileErrorObserver () {
        fileViewModel.getFileError.observe(this) {
            Snackbar.make(fileViewBinding.root, it.message!!, Snackbar.LENGTH_SHORT).show()
            fileViewBinding.progressBar.visibility = View.GONE
            finish()
        }
    }
}