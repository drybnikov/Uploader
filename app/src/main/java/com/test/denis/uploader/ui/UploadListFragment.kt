package com.test.denis.uploader.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.test.denis.uploader.R
import com.test.denis.uploader.di.AbstractViewModelFactory
import com.test.denis.uploader.di.Injectable
import com.test.denis.uploader.model.UploadModel
import com.test.denis.uploader.viewmodel.UploadsViewModel
import kotlinx.android.synthetic.main.uploads_list_layout.*
import javax.inject.Inject

class UploadListFragment : Fragment(), Injectable {

    @Inject
    lateinit var factory: AbstractViewModelFactory<UploadsViewModel>


    private lateinit var viewModel: UploadsViewModel

    private var uploadListAdapter = UploadListAdapter {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.uploads_list_layout, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activity?.let {
            viewModel = ViewModelProviders.of(it, factory).get(UploadsViewModel::class.java)
        }

        initRecyclerView()
        initViewModel()
    }

    private fun initRecyclerView() {
        contentList.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            setHasFixedSize(true)
            adapter = uploadListAdapter
        }
    }

    private fun initViewModel() {
        viewModel.uploadingData().observe(this, Observer { onDataLoaded(it) })

        viewModel.onFileSelectedData.observe(this, Observer {
            Log.d("UploadListFragment", "onFileSelectedData : $it")
        })
    }

    private fun onDataLoaded(data: List<UploadModel>) {
        Log.d("UploadListFragment", "onDataLoaded : $data")
        uploadListAdapter.initData(data)

        contentHolder.text="Proccessing uploads: 0/${data.size}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}