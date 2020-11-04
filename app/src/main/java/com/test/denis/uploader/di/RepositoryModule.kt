package com.test.denis.uploader.di

import com.test.denis.uploader.network.UploadManager
import com.test.denis.uploader.network.UploadRepository
import com.test.denis.uploader.viewmodel.UploadsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val RepositoryDependency = module {

    single { UploadManager(get()) }

    single { UploadRepository(get()) }

    viewModel { UploadsViewModel(get()) }
}