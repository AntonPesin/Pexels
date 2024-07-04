package com.projects.pexels_app.data.network.repository

import android.util.Log
import com.projects.pexels_app.data.Retrofit
import com.projects.pexels_app.data.network.models.CollectionModel
import com.projects.pexels_app.data.network.models.MediaModel
import javax.inject.Inject


class Repository @Inject constructor() {

    suspend fun getCollections(): List<CollectionModel> {
        Log.d("getCollections", "${Retrofit.search.getCollections().collections}")
        return  Retrofit.search.getCollections().collections
    }


    suspend fun getCuratedPhotos(): List<MediaModel> {
        Log.d("getCuratedPhotos", "${Retrofit.search.getCuratedPhotos().photos.size}")
        return Retrofit.search.getCuratedPhotos().photos
    }

    suspend fun searchPhotos(keyword: String, page: Int): List<MediaModel> {
        Log.d("searchPhotos", "Fetching photos for collection: $keyword, page: $page")
        return Retrofit.search.searchPhotos(keyword, page,30).photos
    }

    suspend fun getPhoto(id: Int): MediaModel {
        return Retrofit.search.getPhoto(id)
    }
}