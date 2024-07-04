package com.projects.pexels_app.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.projects.pexels_app.App
import com.projects.pexels_app.data.network.models.MediaModel
import com.projects.pexels_app.data.network.models.Src
import com.projects.pexels_app.data.network.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
@HiltViewModel
class DetailsViewModel @Inject constructor(
    application: Application,
    private val repository: Repository,
) : AndroidViewModel(application) {

    val photoDao = App.INSTANCE.photoDataBase.photoDao()

    suspend fun getPhotoFromApi(id: Int?): MediaModel {
        return repository.getPhoto(id!!)
    }

    fun addPhoto(id: Int, photographer: String, medium: String) {
        val src = Src(medium)
        val mediaModel = MediaModel(id, photographer, src)
        viewModelScope.launch(Dispatchers.IO) {
            photoDao.insert(mediaModel)
        }
    }

    fun deletePhoto(photoId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            photoDao.delete(photoId)
        }
    }

    suspend fun photoExists(photoId: Int?): Boolean {
        return withContext(Dispatchers.IO) {
            photoDao.exists(photoId)
        }
    }
}