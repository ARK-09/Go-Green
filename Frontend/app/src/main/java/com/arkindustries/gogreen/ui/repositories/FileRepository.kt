package com.arkindustries.gogreen.ui.repositories

import android.webkit.MimeTypeMap
import com.arkindustries.gogreen.api.response.ApiResponse
import com.arkindustries.gogreen.api.response.FileResponse
import com.arkindustries.gogreen.api.response.FilesResponse
import com.arkindustries.gogreen.api.services.FileService
import com.arkindustries.gogreen.database.dao.AttachmentDao
import com.arkindustries.gogreen.database.entites.AttachmentEntity
import com.arkindustries.gogreen.utils.handleApiCall
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.io.Files.getFileExtension
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.source
import java.io.File


class FileRepository(
    private val fileService: FileService,
    private val attachmentDao: AttachmentDao,
    private val SEGMENT_SIZE: Long = 500L
) {
    suspend fun uploadFilesAtServer(
        files: List<Pair<String, File>>,
        progressCallback: (fileId: String, progress: Int) -> Unit
    ): ApiResponse<FilesResponse> {
        val fileParts = files.map { (fileId, file) ->
            val requestBody = createProgressRequestBody(file, fileId, progressCallback)
            MultipartBody.Part.createFormData("files", file.name, requestBody)
        }
        return handleApiCall { fileService.uploadFiles(fileParts) }
    }

    private fun createProgressRequestBody(
        file: File,
        fileId: String,
        progressCallback: (fileId: String, progress: Int) -> Unit
    ): RequestBody {
        return object : RequestBody() {
            override fun contentType(): MediaType? {
                val fileExtension: String = getFileExtension(file.path)
                return MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension)
                    ?.toMediaTypeOrNull()
            }

            override fun contentLength(): Long {
                return file.length()
            }

            override fun writeTo(sink: BufferedSink) {
                val source = file.source()
                val buffer = Buffer()
                var uploadedBytes = 0L
                var bytesRead: Long

                while (source.read(buffer, SEGMENT_SIZE).also { bytesRead = it } != -1L) {
                    sink.write(buffer, bytesRead)
                    uploadedBytes += bytesRead
                    val progress = ((uploadedBytes.toFloat() / file.length()) * 100).toInt()
                    progressCallback(fileId, progress)
                }
            }
        }
    }

    suspend fun getFileByIdFromServer(fileId: String): ApiResponse<FileResponse> {
        return handleApiCall { fileService.getFileById(fileId) }
    }

    suspend fun deleteFileByIdAtServer(fileId: String): ApiResponse<String> {
        return handleApiCall { fileService.deleteFileById(fileId) }
    }

    suspend fun upsertFilesAtLocal(files: List<AttachmentEntity>) {
        return attachmentDao.upsertAttachments(files)
    }

    suspend fun getFileByIdFromLocal(id: String): AttachmentEntity {
        return attachmentDao.getAttachmentById(id)
    }

    suspend fun deleteFileByIdFromLocal(id: String) {
        return attachmentDao.deleteById(id)
    }

    suspend fun deleteAllFilesFromLocal() {
        return attachmentDao.deleteAllAttachments()
    }
}
