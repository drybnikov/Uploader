package com.test.denis.uploader.ui

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.test.denis.uploader.R
import com.test.denis.uploader.model.UploadModel
import com.test.denis.uploader.model.UploadStatus
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.item_upload.view.*

class UploadListAdapter(
    var callback: ((UploadModel) -> Unit)
) : RecyclerView.Adapter<CarItemViewHolder>() {

    private val items: MutableList<UploadModel> = mutableListOf()

    fun initData(listItems: List<UploadModel>) {
        items.clear()
        items.addAll(listItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_upload, parent, false)

        return CarItemViewHolder(itemView)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(viewHolder: CarItemViewHolder, position: Int) {
        val listItemModel = items[position]
        viewHolder.bind(listItemModel, callback)
    }

    /*override fun onViewAttachedToWindow(holder: CarItemViewHolder) {
        super.onViewAttachedToWindow(holder)

        holder.subscribeToUploadingUpdates()
    }*/

    override fun onViewDetachedFromWindow(holder: CarItemViewHolder) {
        super.onViewDetachedFromWindow(holder)

        holder.onDetach()
    }
}

class CarItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val disposables = CompositeDisposable()
    private lateinit var model: UploadModel

    fun bind(listItemModel: UploadModel, callback: (UploadModel) -> Unit) {
        model = listItemModel
        with(listItemModel) {
            itemView.fileName.text = "$name"

            itemView.setOnClickListener {
                callback.invoke(listItemModel)
            }

            //TODO SET init values
        }


        subscribeToUploadingUpdates()
        Log.w("CarItemViewHolder", "bind this:$this, model:$model")
    }

    fun subscribeToUploadingUpdates() {
        model.status
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(::onStatusUpdate)
            ?.addTo(disposables)

        model.progress
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(::onProgressUpdate)
            ?.addTo(disposables)
    }

    fun onDetach() {
        Log.w("CarItemViewHolder", "onDetach this:$this")
        disposables.clear()
    }

    private fun onProgressUpdate(progress: Float) {
        itemView.uploadProgress.progress = progress.toInt()
    }

    private fun onStatusUpdate(status: UploadStatus) {
        itemView.uploadStatusText.text = status.name

        itemView.uploadStatus.setImageResource(
            when (status) {
                UploadStatus.UPLOADED -> R.drawable.ic_check_circle_24px
                UploadStatus.FAILED -> R.drawable.ic_error_outline_24px
                else -> R.drawable.ic_cancel_24px
            }
        )
    }
}