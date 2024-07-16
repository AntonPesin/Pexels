package com.projects.pexels_app.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.projects.pexels_app.data.repositories.ConnectivityRepository
import com.projects.pexels_app.domain.models.Collection
import com.projects.pexels_app.domain.models.Photo
import com.projects.pexels_app.usecases.home.collections.ILoadCollectionsUseCase
import com.projects.pexels_app.usecases.home.curated.IGetCuratedPhotosUseCase
import com.projects.pexels_app.usecases.home.search.ISearchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getCuratedPhotosUseCase: IGetCuratedPhotosUseCase,
    private val searchUseCase: ISearchUseCase,
    connectivityRepository: ConnectivityRepository,
    private val loadCollectionUseCase: ILoadCollectionsUseCase,
) : ViewModel() {

    private val _isLoadingCollections = MutableStateFlow(true)
    val isLoadingCollections: StateFlow<Boolean> = _isLoadingCollections.asStateFlow()

    private val _collections = MutableStateFlow<List<Collection>>(emptyList())
    val collections: StateFlow<List<Collection>> get() = _collections

    private val _errorMessage = MutableStateFlow<String?>(null)
    val isConnected: StateFlow<Boolean> = connectivityRepository.isConnected as StateFlow<Boolean>
    suspend fun loadCollections() {
        viewModelScope.launch {
            try {
                _isLoadingCollections.value = true
                _collections.value = loadCollectionUseCase.loadCollections()
            } catch (error: Throwable) {
                _errorMessage.value = "${error.message}"
            } finally {
                _isLoadingCollections.value = false
            }
        }
    }

    suspend fun getCuratedPhotos(): Flow<PagingData<Photo>> {
        return getCuratedPhotosUseCase.getCuratedPhotos(viewModelScope)
            .catch { e ->
                _errorMessage.value = "${e.message}"
                throw e
            }
    }

    suspend fun getSearchResults(query: String): Flow<PagingData<Photo>> {
        return searchUseCase.getSearchResultStream(query, viewModelScope)
            .catch { e ->
                _errorMessage.value = "${e.message}"
                throw e
            }
    }

}


