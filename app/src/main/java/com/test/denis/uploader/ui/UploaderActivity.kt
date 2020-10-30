package com.test.denis.uploader.ui

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.R.attr
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.test.denis.uploader.R
import com.test.denis.uploader.di.AbstractViewModelFactory
import com.test.denis.uploader.di.Injectable
import com.test.denis.uploader.util.FilenameUtils
import com.test.denis.uploader.util.fileSize
import com.test.denis.uploader.util.filename
import com.test.denis.uploader.util.setVisibility
import com.test.denis.uploader.viewmodel.UploadsViewModel
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import kotlinx.android.synthetic.main.activity_uploader.*
import javax.inject.Inject


class UploaderActivity : AppCompatActivity(), Injectable, HasSupportFragmentInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var factory: AbstractViewModelFactory<UploadsViewModel>

    private lateinit var viewModel: UploadsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_uploader)

        initViewModel()

        if (savedInstanceState == null) {
            openUploadList()
        }

        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.upload -> {
                    doPickFile()
                    true
                }

                else -> false
            }
        }
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this, factory).get(UploadsViewModel::class.java)
    }

    override fun onStart() {
        super.onStart()

        viewModel.loadingProgress.observe(this, Observer { toggleLoadingIndicatorVisibility(it) })
    }


    private fun openUploadList() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.listContainer, UploadListFragment())
            .commit()
    }

    private fun showError(errorMessage: String) {
        Snackbar.make(rootView, errorMessage, Snackbar.LENGTH_INDEFINITE)
            .apply {
                setAction(R.string.retry) { toggleLoadingIndicatorVisibility(false) }
                show()
            }
    }

    @SuppressLint("NewApi")
    private fun doPickFile() {
        askForPermissions()

        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            type = "*/*"
        }

        startActivityForResult(intent, OPEN_FILE_CODE)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun askForPermissions() {
        requestPermissions(readPermissions.toTypedArray(), READ_PERMISSIONS_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == OPEN_FILE_CODE) and (resultCode == Activity.RESULT_OK)) {
            if (data?.clipData != null) {
                val count = data.clipData?.itemCount ?: 0
                for (i in 0 until count) {
                    val imageUri = data.clipData?.getItemAt(i)?.uri
                    Log.d("UploaderActivity", "imageUri: $imageUri")
                    //do something with the image (save it to some directory or whatever you need to do with it here)
                }
            } else {
                Log.d("UploaderActivity", "onActivityResult path: ${data?.data?.path}")

                val path = data?.data?.toString() ?: return

                checkFileSize(path) { onFileSelectedSuccess(path) }
            }
        }
    }

    private fun checkFileSize(path: String, onSuccess: () -> Unit) {
        val uri = Uri.parse(path)
        val fileSize = uri.fileSize(this) ?: 0L
        val isTooBig = fileSize > MAX_UPLOAD_FILE_SIZE

        if (isTooBig) {
            showError("Please select one that is no larger than 10mb.")
            return
        }

        onSuccess()
    }

    private fun onFileSelectedSuccess(path: String) {
        val uri = Uri.parse(path)
        val filename = uri.filename(this).orEmpty()
        val realPath = FilenameUtils.getRealPath(this, uri).toString()

        Log.d("UploaderActivity", "onFileSelectedSuccess filename: $filename, realPath:$realPath")
        viewModel.onFileSelected(filename, realPath)
    }

    private fun toggleLoadingIndicatorVisibility(isVisible: Boolean) {
        loadingProgress.setVisibility(isVisible)
    }


    override fun supportFragmentInjector() = dispatchingAndroidInjector

    companion object {
        private const val MEGA_BYTE = 1048576L
        private const val MAX_UPLOAD_FILE_SIZE = 10 * MEGA_BYTE

        const val OPEN_FILE_CODE = 123
        const val READ_PERMISSIONS_CODE = 124
        const val APP_SETTINGS = 125

        val readPermissions = listOf(READ_EXTERNAL_STORAGE)
    }
}