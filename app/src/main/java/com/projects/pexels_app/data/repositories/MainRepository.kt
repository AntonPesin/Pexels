package com.projects.pexels_app.data.repositories

import androidx.paging.PagingData
import com.projects.pexels_app.domain.models.Collection
import com.projects.pexels_app.domain.NetworkResult
import com.projects.pexels_app.domain.models.Photo
import kotlinx.coroutines.flow.Flow

interface MainRepository {
    suspend fun getCollections(): NetworkResult<List<Collection>>
    suspend fun getCuratedPhotos(): NetworkResult<Flow<PagingData<Photo>>>
    suspend fun getSearchResult(query: String): NetworkResult<Flow<PagingData<Photo>>>
    suspend fun getPhotoById(id: Int): NetworkResult<Photo>
}