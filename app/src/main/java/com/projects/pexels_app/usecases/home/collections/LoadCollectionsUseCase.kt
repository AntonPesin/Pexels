package com.projects.pexels_app.usecases.home.collections

import com.projects.pexels_app.R
import com.projects.pexels_app.data.repositories.MainRepository
import com.projects.pexels_app.domain.models.Collection
import com.projects.pexels_app.domain.NetworkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoadCollectionsUseCase(private val repository: MainRepository) : ILoadCollectionsUseCase {
    override suspend fun loadCollections(): List<Collection> {
        return withContext(Dispatchers.IO) {
            try {
                when (val result = repository.getCollections()) {
                    is NetworkResult.Success -> result.data
                    is NetworkResult.Error -> throw Exception("${R.string.error_tag}: ${result.errorMsg}")
                }
            } catch (e: Exception) {
                throw e
            }
        }
    }
}