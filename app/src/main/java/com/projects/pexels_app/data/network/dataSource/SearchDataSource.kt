package com.projects.pexels_app.data.network.dataSource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.projects.pexels_app.data.network.models.MediaModel
import com.projects.pexels_app.data.network.repository.Repository

class SearchDataSource(private val repository: Repository, private val keyword: String) :
    PagingSource<Int, MediaModel>() {

    override fun getRefreshKey(state: PagingState<Int, MediaModel>): Int? {
        return state.anchorPosition?.let {
            state.closestPageToPosition(it)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
        }
    }
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MediaModel> {
        try {
            val page = params.key ?: 1
            val photos = repository.searchPhotos(keyword, page)
            return LoadResult.Page(
                data = photos,
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (photos.isEmpty()) null else page + 1
            )
        } catch (exception: Exception) {
            return LoadResult.Error(exception)
        }
    }


}