package com.test.denis.uploader.network

import androidx.lifecycle.MutableLiveData
import com.test.denis.uploader.model.UploadModel
import com.test.denis.uploader.model.UploadStatus
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class UploadRepository(private val uploadManager: UploadManager) {

    val uploadingData = MutableLiveData<List<UploadModel>>(mutableListOf())

    suspend fun addUploadFile(uploadModel: UploadModel): UploadStatus {
        //val uploadManager = uploadManagerProvider.get()
        val uploadingList = uploadingData.value?.toMutableList() ?: mutableListOf()

        val uploadingProgress = uploadManager.uploadProgress(uploadModel.id.hashCode())
            .debounce(50, TimeUnit.MILLISECONDS, Schedulers.io())

        val streamedUploadModel = uploadModel.apply {
            status = uploadManager.uploadStatus(uploadModel.id.hashCode())
            progress = uploadingProgress
        }
        uploadingList.add(0, streamedUploadModel)

        uploadingData.postValue(uploadingList)

        val status = uploadManager.uploadFile(uploadModel)
        val uploadIndex = uploadingList.indexOf(streamedUploadModel)
        uploadingList[uploadIndex] = streamedUploadModel.copy(uploadStatus = status)

        uploadingData.postValue(uploadingList)

        return status
    }
}