package com.projects.pexels_app.usecases.home.curated

import androidx.paging.PagingData
import com.projects.pexels_app.R
import com.projects.pexels_app.data.repositories.MainRepository
import com.projects.pexels_app.domain.NetworkResult
import com.projects.pexels_app.domain.models.Photo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

class CuratedPhotosUseCase(private val repository: MainRepository): IGetCuratedPhotosUseCase {
    private var currentSearchResult: Flow<PagingData<Photo>>? = null

    override suspend fun getCuratedPhotos(scope: CoroutineScope): Flow<PagingData<Photo>> {
        currentSearchResult?.let { return it }

        val newResult = try {
            when (val result = repository.getCuratedPhotos()) {
                is NetworkResult.Success -> result.data
                is NetworkResult.Error -> throw Exception("${R.string.error_tag}: ${result.code}, ${result.errorMsg}")
            }
        } catch (e: Exception) {
            throw e
        }

        currentSearchResult = newResult

        return newResult
    }


}