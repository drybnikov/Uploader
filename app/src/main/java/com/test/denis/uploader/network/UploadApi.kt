package com.test.denis.uploader.network

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UploadApi {
    @Multipart
    @POST("/testUpload")
    fun uploadFile(
        @Part upload: MultipartBody.Part,
        @Part data:  MultipartBody.Part
    ): Call<Void>
}