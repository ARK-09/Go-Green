package com.arkindustries.gogreen.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkindustries.gogreen.api.response.ApiResponse
import com.arkindustries.gogreen.database.entites.AttachmentEntity
import com.arkindustries.gogreen.ui.repositories.FileRepository
import kotlinx.coroutines.launch
import java.io.File

class FileViewModel(private val fileRepository: FileRepository) : ViewModel() {
    private val _uploadResult = MutableLiveData<List<AttachmentEntity>>()
    val uploadResult: LiveData<List<AttachmentEntity>> get() = _uploadResult

    private val _getFileResult = MutableLiveData<AttachmentEntity>()
    val getFileResult: LiveData<AttachmentEntity> get() = _getFileResult

    private val _deleteFileResult = MutableLiveData<String>()
    val deleteFileResult: LiveData<String> get() = _deleteFileResult

    private val _uploadProgressMap = MutableLiveData<Pair<String, Int>>()
    val uploadProgressMap: LiveData<Pair<String, Int>> get() = _uploadProgressMap

    private val _uploadError = MutableLiveData<ApiResponse<*>>()
    val uploadError: LiveData<ApiResponse<*>> = _uploadError

    private val _deleteFileError = MutableLiveData<ApiResponse<*>>()
    val deleteFileError: LiveData<ApiResponse<*>> = _deleteFileError

    private val _getFileError = MutableLiveData<ApiResponse<*>>()
    val getFileError: LiveData<ApiResponse<*>> = _getFileError


    fun uploadFiles(files: List<Pair<String, File>>) {
        viewModelScope.launch {
            val response = fileRepository.uploadFilesAtServer(files) { fileId, progress ->
                _uploadProgressMap.postValue(Pair(fileId, progress))
            }

            if (response.status == "fail" || response.status == "error") {
                _uploadError.value = response
                return@launch
            }

            val attachments = response.data!!.files.map { file ->
                return@map AttachmentEntity (file.id, file.mimeType, file.originalName, file.createdDate, file.url)
            }

            fileRepository.upsertFilesAtLocal(attachments)
            _uploadResult.value = attachments
        }
    }

    fun deleteFile (id: String) {
        viewModelScope.launch {
            val response = fileRepository.deleteFileByIdAtServer(id)

            if (response.status == "fail" || response.status == "error") {
                _deleteFileError.value = ApiResponse<Any>(response.status, response.code, response.message, response.stack, id)
                return@launch
            }
            fileRepository.deleteFileByIdFromLocal(id)
            _deleteFileResult.value = id
        }
    }

    fun getFile (id: String) {
        viewModelScope.launch {
            val response = fileRepository.getFileByIdFromServer(id)

            if (response.status == "fail" || response.status == "error") {
                _getFileError.value = response
                return@launch
            }
            fileRepository.deleteFileByIdFromLocal(id)
            val attachment = response.data!!.file
            val attachmentEntity = AttachmentEntity(attachment.id, attachment.mimeType, attachment.originalName, attachment.createdDate, attachment.url)
            fileRepository.upsertFilesAtLocal(mutableListOf(attachmentEntity))
            _getFileResult.value = attachmentEntity
        }
    }
}
