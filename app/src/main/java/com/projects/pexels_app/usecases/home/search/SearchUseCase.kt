package com.projects.pexels_app.usecases.home.search

import androidx.paging.PagingData
import com.projects.pexels_app.R
import com.projects.pexels_app.data.repositories.MainRepository
import com.projects.pexels_app.domain.NetworkResult
import com.projects.pexels_app.domain.models.Photo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

class SearchUseCase(private val repository: MainRepository) : ISearchUseCase {
    private var currentQueryValue: String? = null

    private var currentSearchResult: Flow<PagingData<Photo>>? = null


    override suspend fun getSearchResultStream(
        query: String,
        scope: CoroutineScope,
    ): Flow<PagingData<Photo>> {
        if (query == currentQueryValue && currentSearchResult != null) {
            return currentSearchResult!!
        }
        currentQueryValue = query

        val newResult = try {
            when (val result = repository.getSearchResult(query)) {
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