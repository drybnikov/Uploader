package com.test.denis.uploader.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.test.denis.uploader.model.UploadModel
import com.test.denis.uploader.model.UploadStatus
import com.test.denis.uploader.network.UploadRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
        val uploadModel =
            UploadModel(name.hashCode().toString(), name, path, UploadStatus.WAITING, null, null);

        viewModelScope.launch(Dispatchers.IO) {
            repository.addUploadFile(uploadModel)

            //TODO Update values
            _fileSelectionData.postValue(uploadModel.copy(uploadStatus = UploadStatus.UPLOADED))
        }
    }
}