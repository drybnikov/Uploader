package com.test.denis.uploader.model

import io.reactivex.Observable

data class UploadModel(
    val id: String,
    val name: String,
    val path: String,
    val uploadStatus: UploadStatus,
    var status: Observable<UploadStatus>?,
    var progress: Observable<Float>?
)

enum class UploadStatus{ WAITING, UPLOADED, UPLOADING, FAILED }