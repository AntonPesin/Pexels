package com.projects.pexels_app.usecases.home.curated

import androidx.paging.PagingData
import com.projects.pexels_app.domain.models.Photo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface IGetCuratedPhotosUseCase {
   suspend fun getCuratedPhotos(scope: CoroutineScope): Flow<PagingData<Photo>>
}