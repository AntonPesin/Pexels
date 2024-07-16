package com.projects.pexels_app.data.repositories

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.projects.pexels_app.R
import com.projects.pexels_app.data.api.ApiService
import com.projects.pexels_app.data.pagingSource.CuratedPagingSource
import com.projects.pexels_app.data.pagingSource.SearchPagingSource
import com.projects.pexels_app.domain.models.Collection
import com.projects.pexels_app.domain.NetworkResult
import com.projects.pexels_app.domain.models.Photo
import kotlinx.coroutines.flow.Flow


class RepositoryImpl(private val apiService: ApiService) : MainRepository {
    override suspend fun getCollections(): NetworkResult<List<Collection>> {
        return try {
            val response = apiService.getCollections()
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!.collections)
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Error(429,"${R.string.error_429}")
        }
    }

    override suspend fun getCuratedPhotos(): NetworkResult<Flow<PagingData<Photo>>> {
        return try {
            val pager = Pager(
                config = PagingConfig(
                    pageSize = 30,
                    enablePlaceholders = false,
                    prefetchDistance = 10,
                    initialLoadSize = 30
                ),
                pagingSourceFactory = { CuratedPagingSource(service = apiService) }
            )
            val flow = pager.flow
            NetworkResult.Success(flow)
        } catch (e: Exception) {
            NetworkResult.Error(429, "${R.string.error_429}")
        }
    }

    override suspend fun getSearchResult(query: String): NetworkResult<Flow<PagingData<Photo>>> {
        return try {
            val pager = Pager(
                config = PagingConfig(
                    pageSize = 30,
                    prefetchDistance = 10,
                    initialLoadSize = 30,
                    enablePlaceholders = false
                ), pagingSourceFactory = { SearchPagingSource(apiService, query) })
            val flow = pager.flow
            NetworkResult.Success(flow)
        } catch (e: Exception) {
            NetworkResult.Error(429, "${R.string.error_429}")
        }
    }

    override suspend fun getPhotoById(id: Int): NetworkResult<Photo> {
        return try {
            val response = apiService.getPhotoById(id)
            if (response.isSuccessful && response.body() != null) {
                NetworkResult.Success(response.body()!!)
            } else {
                NetworkResult.Error(response.code(), response.message())
            }
        } catch (e: Exception) {
            NetworkResult.Error(429, "${R.string.error_429}")
        }
    }
}