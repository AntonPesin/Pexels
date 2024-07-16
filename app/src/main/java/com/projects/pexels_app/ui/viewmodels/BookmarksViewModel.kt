package com.projects.pexels_app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.projects.pexels_app.data.db.PhotoDataBase
import com.projects.pexels_app.domain.models.Photo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
@HiltViewModel
class BookmarksViewModel @Inject constructor(

    private val dataBase: PhotoDataBase,
) : ViewModel() {

    suspend fun hasPhotos(): Boolean {
        return withContext(Dispatchers.IO) {
            dataBase.photoDao().getPhotoCount() > 0
        }
    }

    fun getPagingPhotos(): Flow<PagingData<Photo>> {
        return Pager(
            PagingConfig(pageSize = 30),
            pagingSourceFactory = { dataBase.photoDao().getPagingPhotos() }
        ).flow.cachedIn(viewModelScope)
    }

}