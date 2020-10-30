package com.test.denis.uploader.network

import androidx.lifecycle.MutableLiveData
import com.test.denis.uploader.model.UploadModel
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Provider

class UploadRepository @Inject constructor(private val uploadManagerProvider: Provider<UploadManager>) {

    val uploadingData = MutableLiveData<List<UploadModel>>(mutableListOf())

    suspend fun addUploadFile(uploadModel: UploadModel) {
        val uploadManager = uploadManagerProvider.get()
        val uploadingList = uploadingData.value?.toMutableList() ?: mutableListOf()

        val uploadingProgress = uploadManager.uploadProgress(uploadModel.id.hashCode())
            .debounce(50, TimeUnit.MILLISECONDS, Schedulers.io())

        val streamedUploadModel = uploadModel.apply {
            status = uploadManager.uploadStatus(uploadModel.id.hashCode())
            progress = uploadingProgress
        }
        uploadingList.add(0, streamedUploadModel)

        uploadingData.postValue(uploadingList)

        uploadManager.uploadFile(uploadModel)
    }
}