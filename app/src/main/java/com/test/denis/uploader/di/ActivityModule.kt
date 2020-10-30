package com.test.denis.uploader.di

import com.test.denis.uploader.ui.UploaderActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityModule {
    @ContributesAndroidInjector(modules = [FragmentBuildersModule::class])
    abstract fun contributeUploaderActivity(): UploaderActivity
}