package com.arkindustries.gogreen.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.DefaultLifecycleObserver


class FilePickerUtil(
    private val context: Context,
    private val allowedFileTypes: Array<String>,
    private val allowMultiple: Boolean = true
    ) : DefaultLifecycleObserver {
    private lateinit var onFilesResult: (List<Uri>) -> Unit

    fun pickFilesUsingLauncher(filePickerLauncher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, allowedFileTypes)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultiple)
        }
        filePickerLauncher.launch(intent)
    }

    fun getFiles (onFilesResult: (List<Uri>) -> Unit) {
        this.onFilesResult = onFilesResult
    }

    fun handleFilePickerResult(result: ActivityResult) {
        val resultCode = result.resultCode
        val data = result.data

        if (resultCode == ComponentActivity.RESULT_OK && data != null) {
            val selectedFiles = mutableListOf<Uri>()

            val clipData = data.clipData
            if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    val selectedFileUri = clipData.getItemAt(i).uri
                    if (isAllowedFileType(selectedFileUri)) {
                        selectedFiles.add(selectedFileUri)
                    }
                }
            }

            val singleUri = data.data
            if (singleUri != null && isAllowedFileType(singleUri)) {
                selectedFiles.add(singleUri)
            }

            onFilesResult.invoke(selectedFiles)
        }
    }

    private fun isAllowedFileType(uri: Uri): Boolean {
        val fileType = context.contentResolver.getType(uri)

        if (fileType != null) {
            for (allowedType in allowedFileTypes) {
                val regexPattern = allowedType.replace("*", ".*")
                if (fileType.matches(regexPattern.toRegex())) {
                    return true
                }
            }
        }
        return false
    }

    fun getOriginalFileName(uri: Uri): String? {
        var fileName: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)

        if (cursor != null && cursor.moveToFirst()) {
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (columnIndex != -1) {
                    fileName = cursor.getString(columnIndex)
                }
            }
            cursor.close()
        }

        return fileName
    }

    fun getFileMimeType(uri: Uri): String? {
        return context.contentResolver.getType(uri)
    }
}