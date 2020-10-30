package com.test.denis.uploader.network

import androidx.lifecycle.MutableLiveData
import com.test.denis.uploader.model.UploadModel
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class UploadRepository @Inject constructor(private val uploadManager: UploadManager) {

    val uploadingData = MutableLiveData<List<UploadModel>>(mutableListOf())

    fun addUploadFile(uploadModel: UploadModel) {
        val uploadingList = uploadingData.value?.toMutableList() ?: mutableListOf()

        val uploadingProgress = uploadManager.uploadProgress(uploadModel.id.hashCode())
            .debounce(100, TimeUnit.MILLISECONDS, Schedulers.io())

        val streamedUploadModel = uploadModel.apply {
            status = uploadManager.uploadStatus(uploadModel.id.hashCode())
            progress = uploadingProgress
        }
        uploadingList.add(0, streamedUploadModel)

        uploadingData.value = uploadingList

        uploadManager.uploadFile(uploadModel)
    }
}