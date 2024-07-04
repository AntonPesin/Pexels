package com.projects.pexels_app.ui.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.projects.pexels_app.R
import com.projects.pexels_app.data.network.models.MediaModel
import com.projects.pexels_app.databinding.PhotoItemBinding

class PhotoAdapter : PagingDataAdapter<MediaModel, PhotosViewHolder>(PhotoDiffUtilCallBack()) {

    private var photoListener: PhotoClickListener? = null

    fun setOnImageClickListener(listener: PhotoClickListener) {
        photoListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotosViewHolder {
        return PhotosViewHolder(
            PhotoItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PhotosViewHolder, position: Int) {
        val photoData = getItem(position)
        Log.d("adapter","$photoData")
        with(holder.binding) {
            photo.setOnClickListener {
                photoListener?.onPhotoClick(photoData)
            }
            Glide.with(photo.context)
                .load(photoData?.src?.medium)
                .error(R.drawable.placeholder)
                .transform(FitCenter(), RoundedCorners(30))
                .into(holder.binding.photo)
        }
    }



    interface PhotoClickListener {
        fun onPhotoClick(photo: MediaModel?) {
        }
    }
}

class PhotosViewHolder(val binding: PhotoItemBinding) :
    RecyclerView.ViewHolder(binding.root)

class PhotoDiffUtilCallBack : DiffUtil.ItemCallback<MediaModel>() {
    override fun areItemsTheSame(oldItem: MediaModel, newItem: MediaModel): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: MediaModel, newItem: MediaModel): Boolean =
        oldItem == newItem
}

