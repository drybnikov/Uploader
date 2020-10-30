package com.test.denis.uploader.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.test.denis.uploader.model.UploadModel
import com.test.denis.uploader.model.UploadStatus
import com.test.denis.uploader.network.UploadRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import java.io.File
import javax.inject.Inject

class UploadsViewModel @Inject constructor(private val repository: UploadRepository) : ViewModel() {

    private val disposables = CompositeDisposable()

    private val _fileSelectionData = MutableLiveData<UploadModel>()
    val onFileSelectedData: LiveData<UploadModel> = _fileSelectionData

    val loadingProgress = MutableLiveData<Boolean>(false)

    fun uploadingData(): LiveData<List<UploadModel>> = repository.uploadingData

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    fun onFileSelected(name: String, path: String) {
        val uploadModel = UploadModel(name.hashCode().toString(), name, path, UploadStatus.WAITING, null, null);
        repository.addUploadFile(uploadModel)

        _fileSelectionData.postValue(uploadModel)
    }

    private fun onSuccess() {
        loadingProgress.value = false
    }

    private fun onError(error: Throwable) {
        Log.w("UploadsViewModel", "onError", error)

        loadingProgress.value = false
    }
}