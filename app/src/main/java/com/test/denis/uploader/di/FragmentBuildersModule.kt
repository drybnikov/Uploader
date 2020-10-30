package com.test.denis.uploader.di

import com.test.denis.uploader.ui.UploadListFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBuildersModule {
    @ContributesAndroidInjector
    abstract fun contributeCarsMapFragment(): UploadListFragment
}