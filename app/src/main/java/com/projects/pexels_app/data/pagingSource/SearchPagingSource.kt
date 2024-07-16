package com.projects.pexels_app.data.pagingSource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.projects.pexels_app.R
import com.projects.pexels_app.data.api.ApiService
import com.projects.pexels_app.domain.models.Photo
import com.projects.pexels_app.utils.Constant
import retrofit2.HttpException
import java.io.IOException

class SearchPagingSource(private val apiService: ApiService, private val query: String) :
    PagingSource<Int, Photo>() {

    override fun getRefreshKey(state: PagingState<Int, Photo>): Int? {
        return state.anchorPosition?.let {
            state.closestPageToPosition(it)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(it)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Photo> {
        return try {
            val position = params.key ?: Constant.STARTING_PAGE_INDEX
            val response = apiService.searchPhotos(query, position)

            if (response.isSuccessful) {
                val photos = response.body()?.photos ?: emptyList()
                return LoadResult.Page(
                    data = photos,
                    prevKey = if (position == Constant.STARTING_PAGE_INDEX) null else position - 1,
                    nextKey = if (photos.isEmpty()) null else position + 1
                )
            } else {
                LoadResult.Error(Exception("${R.string.error_tag}: ${response.code()} ${response.message()}"))
            }
        } catch (exception: IOException) {
            LoadResult.Error(exception)
        } catch (exception: HttpException) {
            LoadResult.Error(exception)
        }
    }
}
