package com.projects.pexels_app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.projects.pexels_app.data.db.PhotoDataBase
import com.projects.pexels_app.data.repositories.ConnectivityRepository
import com.projects.pexels_app.domain.models.Photo
import com.projects.pexels_app.domain.models.Src
import com.projects.pexels_app.usecases.details.IDetailsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val useCase: IDetailsUseCase,
    private val dataBase: PhotoDataBase,
    connectivityRepository: ConnectivityRepository,
) : ViewModel() {

    private val _errorMessage = MutableStateFlow<String?>(null)
    val isConnected: StateFlow<Boolean> = connectivityRepository.isConnected as StateFlow<Boolean>
    suspend fun getPhotoByIdFromDataBase(id: Int): Photo {
        return useCase.getPhotoFromDataBase(id)
    }

    suspend fun getPhotoByIdFromApi(id: Int): Photo {
        return useCase.getPhotoFromApi(id)
    }

    fun addPhoto(id: Int, photographer: String, medium: String) {
        val src = Src(medium)
        val photo = Photo(id, photographer, src)
        viewModelScope.launch(Dispatchers.IO) {
            dataBase.photoDao().insert(photo)
        }
    }

    fun deletePhoto(photoId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dataBase.photoDao().delete(photoId)
        }
    }

    suspend fun photoExists(photoId: Int?): Boolean {
        return withContext(Dispatchers.IO) {
            dataBase.photoDao().exists(photoId)
        }
    }

    fun errorMessage(): StateFlow<String?> {
        return _errorMessage
    }
}