package com.projects.pexels_app.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.projects.pexels_app.R
import com.projects.pexels_app.data.network.models.MediaModel
import com.projects.pexels_app.databinding.PhotoItemBinding

class CuratedPhotoAdapter : RecyclerView.Adapter<CuratedPhotoViewHolder>() {
    private var photoList: List<MediaModel> = emptyList()
    private var photoListener: PhotoClickListener? = null

    fun setData(image: List<MediaModel>) {
        photoList = image.take(30)
        notifyDataSetChanged()
    }



    fun setOnImageClickListener(listener: PhotoClickListener) {
        photoListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CuratedPhotoViewHolder {
        return CuratedPhotoViewHolder(
            PhotoItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CuratedPhotoViewHolder, position: Int) {
        val photoData = photoList[position]
        with(holder.binding) {
            photo.setOnClickListener {
                photoListener?.onPhotoClick(photoData)
            }
            Glide.with(photo.context)
                .load(photoData.src.medium)
                .error(R.drawable.placeholder)
                .transform(FitCenter(), RoundedCorners(30))
                .into(holder.binding.photo)

        }

    }


    override fun getItemCount(): Int {
        return photoList.size
    }

    interface PhotoClickListener {
        fun onPhotoClick(photo: MediaModel?) {
        }
    }
}


class CuratedPhotoViewHolder(val binding: PhotoItemBinding) :
    RecyclerView.ViewHolder(binding.root)