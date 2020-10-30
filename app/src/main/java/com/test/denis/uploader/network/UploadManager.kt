package com.test.denis.uploader.network

import android.util.Log
import android.util.SparseArray
import android.webkit.MimeTypeMap
import com.test.denis.uploader.model.UploadModel
import com.test.denis.uploader.model.UploadStatus
import com.test.denis.uploader.util.getOrPut
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.net.URLEncoder
import java.util.*
import javax.inject.Inject

class UploadManager @Inject constructor(private val uploadApi: UploadApi) {

    private val progressSubject = SparseArray<PublishSubject<Float>>()
    private val statusSubject = SparseArray<BehaviorSubject<UploadStatus>>()

    private var lastWorkId = SparseArray<UUID>()
    private var lastProgressPercentUpdate = 0.0f
    private var uploadId: Int = 0

    suspend fun uploadFile(uploadModel: UploadModel) {
        uploadId = uploadModel.id.hashCode()
        //lastProgressPercentUpdate = 0.0f

        lastWorkId.put(uploadId, UUID.randomUUID())
        Log.w("UploadManager", "uploadFile $uploadModel, uploadId:$uploadId, this:$this")
        try {
            val fileUpload = uploadModel.path.createFilePart("upload", ::progressHandler)
            val dataJson = "{\"data\":{\"name\":\"${uploadModel.name}\"}}"

            val data = dataJson.createFormData("data")

            updateStatus(uploadId, UploadStatus.UPLOADING)

            val response = uploadApi.uploadFile(fileUpload, data)
            if (response.isSuccessful) {
                updateStatus(uploadId, UploadStatus.UPLOADED)
            } else {
                Log.d("upload file", "Error occur:" + response.errorBody().toString())
                updateStatus(uploadId, UploadStatus.FAILED)
            }
        } catch (e: Exception) {
            Log.e("upload file", "Error occur:", e)

            updateStatus(uploadId, UploadStatus.FAILED)
        }
    }

    private fun progressHandler(bytesWritten: Long, contentLength: Long) {
        val progress = (bytesWritten.toFloat() / contentLength.toFloat()) * MAX_PROGRESS
        if (progress - lastProgressPercentUpdate > 1 || (progress == MAX_PROGRESS)) {
            // publish progress
            updateProgress(uploadId, progress)
            lastProgressPercentUpdate = progress
        }
    }

    private fun String.createFilePart(
        name: String,
        handler: ProgressHandler? = null
    ): MultipartBody.Part {
        val bodyFile = File(this)

        val mimeType = MimeTypeMap
            .getSingleton()
            .getMimeTypeFromExtension(bodyFile.extension)
            ?: "application/octet-stream"
        val mediaType = MediaType.parse(mimeType)

        val requestBody = RequestBody.create(mediaType, bodyFile)
        val countingBody =
            if (handler != null) CountingRequestBody(requestBody, handler) else requestBody

        return MultipartBody.Part.createFormData(
            name,
            URLEncoder.encode(bodyFile.name, "UTF-8"),
            countingBody
        )
    }

    private fun String.createRequestBody() =
        RequestBody.create(MediaType.parse(MULTIPART_FORM_DATA), this)

    private fun String.createFormData(name: String) = MultipartBody.Part.createFormData(name, this)

    fun uploadProgress(uploadId: Int): Observable<Float> {
        return progressSubject.getOrPut(uploadId) {
            PublishSubject.create<Float>()
        }
    }

    private fun updateProgress(uploadId: Int, value: Float) {
        progressSubject.get(uploadId)?.onNext(value)
    }

    fun uploadStatus(uploadId: Int): Observable<UploadStatus> =
        statusSubject.getOrPut(uploadId) {
            BehaviorSubject.createDefault(UploadStatus.WAITING)
        }

    private fun updateStatus(uploadId: Int, status: UploadStatus) {
        statusSubject.get(uploadId)?.onNext(status)
        Log.e("upload file", "updateStatus uploadId:$uploadId, status:$status, progress:$lastProgressPercentUpdate")

        if (status == UploadStatus.UPLOADED) {
            uploadingDone(uploadId)
        }
    }

    fun cancelUploading(videoId: Int) {

    }

    private fun uploadingDone(uploadId: Int) {
        progressSubject.get(uploadId)?.onComplete()
        statusSubject.get(uploadId)?.onComplete()

        progressSubject.remove(uploadId)
        statusSubject.remove(uploadId)
    }

    companion object {
        private const val MAX_PROGRESS = 100f

        private const val MULTIPART_FORM_DATA = "multipart/form-data"
    }
}