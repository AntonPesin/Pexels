package com.projects.pexels_app.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.projects.pexels_app.data.network.dataSource.SearchDataSource
import com.projects.pexels_app.data.network.models.CollectionModel
import com.projects.pexels_app.data.network.models.MediaModel
import com.projects.pexels_app.data.network.repository.Repository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val repository: Repository,
) : AndroidViewModel(application) {

    private val _isLoadingCollections = MutableStateFlow(true)
    val isLoadingCollections: StateFlow<Boolean> = _isLoadingCollections.asStateFlow()

    private val _collections = MutableStateFlow<List<CollectionModel>>(emptyList())
    val collections: StateFlow<List<CollectionModel>> get() = _collections

    private val _errorMessage = MutableStateFlow<String?>(null)

    private val _keyword = MutableStateFlow("")
    val keyword: MutableStateFlow<String> = _keyword

     fun loadCollections() {
        viewModelScope.launch {
            try {
                _isLoadingCollections.value = true
                _collections.value = repository.getCollections()
            } catch (error: Throwable) {
                Log.e("MainViewModel load", error.message ?: "")
                _errorMessage.value = " ${error.message}"
            } finally {
                _isLoadingCollections.value = false
            }
        }
    }

    suspend fun getCuratedPhotos(): List<MediaModel>? {
        return try {
            repository.getCuratedPhotos()
        } catch (exception: Exception) {
            if (exception is HttpException && exception.code() == 429) {
                throw exception
            } else {
                _errorMessage.value = exception.message
                null
            }
        }
    }


    val pagedSearchPhotos: Flow<PagingData<MediaModel>> = keyword.flatMapLatest { keyword ->
        Pager(
            config = PagingConfig(pageSize = 30),
            pagingSourceFactory = { SearchDataSource(repository, keyword) }
        ).flow.cachedIn(viewModelScope)
    }

}


