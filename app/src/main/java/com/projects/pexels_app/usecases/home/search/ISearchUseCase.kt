package com.projects.pexels_app.usecases.home.search

import androidx.paging.PagingData
import com.projects.pexels_app.domain.models.Photo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface ISearchUseCase {
        suspend fun getSearchResultStream(query: String, scope: CoroutineScope): Flow<PagingData<Photo>>
}