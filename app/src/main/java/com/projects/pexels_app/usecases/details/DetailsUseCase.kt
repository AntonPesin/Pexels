package com.projects.pexels_app.usecases.details

import com.projects.pexels_app.R
import com.projects.pexels_app.data.repositories.MainRepository
import com.projects.pexels_app.data.db.PhotoDataBase
import com.projects.pexels_app.domain.NetworkResult
import com.projects.pexels_app.domain.models.Photo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DetailsUseCase(private val repository: MainRepository,
                     private val dataBase: PhotoDataBase
): IDetailsUseCase {
    override suspend fun getPhotoFromApi(id: Int): Photo {
        return withContext(Dispatchers.IO) {
            when (val result = repository.getPhotoById(id)) {
                is NetworkResult.Success -> result.data
                is NetworkResult.Error -> throw Exception("${R.string.error_tag}: ${result.code}, ${result.errorMsg}")
            }
        }
    }

    override suspend fun getPhotoFromDataBase(id: Int): Photo {
        return withContext(Dispatchers.IO) {
            dataBase.photoDao().get(id)
        }


    }
}