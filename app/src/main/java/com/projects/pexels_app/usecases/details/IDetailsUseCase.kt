package com.projects.pexels_app.usecases.details

import com.projects.pexels_app.domain.models.Photo

interface IDetailsUseCase {
    suspend fun getPhotoFromApi(id:Int): Photo

    suspend fun getPhotoFromDataBase(id:Int): Photo
}