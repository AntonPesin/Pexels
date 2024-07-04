package com.projects.pexels_app.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.projects.pexels_app.App
import com.projects.pexels_app.data.network.models.MediaModel
import com.projects.pexels_app.data.network.repository.db.dao.PhotoDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class BookmarksViewModel @Inject constructor(
    application: Application,
) : AndroidViewModel(application) {

    private val photoDao: PhotoDao = App.INSTANCE.photoDataBase.photoDao()

    suspend fun hasPhotos(): Boolean {
        return withContext(Dispatchers.IO) {
            photoDao.getPhotoCount() > 0
        }
    }

    fun getPagingPhotos(): Flow<PagingData<MediaModel>> {
        return Pager(
            PagingConfig(pageSize = 30),
            pagingSourceFactory = { photoDao.getPagingPhotos() }
        ).flow.cachedIn(viewModelScope)
    }

}