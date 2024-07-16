package com.projects.pexels_app.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.projects.pexels_app.R
import com.projects.pexels_app.databinding.PhotoItemBinding
import com.projects.pexels_app.domain.models.Photo

class PhotoAdapter(
    private val sourceFragment: String,
    private val onPhotoClick: (Photo?, String) -> Unit,
) : PagingDataAdapter<Photo, PhotosViewHolder>(PhotoDiffUtilCallBack()) {


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
        with(holder.binding) {
            photo.setOnClickListener {
                onPhotoClick(photoData, sourceFragment)
            }
            Glide.with(photo.context)
                .load(photoData?.src?.medium)
                .error(R.drawable.placeholder)
                .placeholder(R.drawable.placeholder)
                .transform( RoundedCorners(30))
                .into(holder.binding.photo)
        }
    }


}

class PhotosViewHolder(val binding: PhotoItemBinding) :
    RecyclerView.ViewHolder(binding.root)

class PhotoDiffUtilCallBack : DiffUtil.ItemCallback<Photo>() {
    override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean =
        oldItem == newItem
}

