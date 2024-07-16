package com.projects.pexels_app.data.api

import com.projects.pexels_app.BuildConfig
import com.projects.pexels_app.data.lists.CollectionsList
import com.projects.pexels_app.data.lists.PhotoList
import com.projects.pexels_app.domain.models.Photo
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @Headers("Authorization: ${BuildConfig.API_KEY}")
    @GET("collections/featured?per_page=7")
    suspend fun getCollections(
    ): Response<CollectionsList>

    @Headers("Authorization: ${BuildConfig.API_KEY}")
    @GET("curated?type=photos&per_page=30")
    suspend fun getCuratedPhotos(
        @Query("page") page: Int,
    ): Response<PhotoList>

    @Headers("Authorization: ${BuildConfig.API_KEY}")
    @GET("search?per_page=30")
    suspend fun searchPhotos(
        @Query("query") query: String,
        @Query("page") page: Int,
    ): Response<PhotoList>


    @Headers("Authorization: ${BuildConfig.API_KEY}")
    @GET("photos/{id}")
    suspend fun getPhotoById(
        @Path("id") id: Int,
    ): Response<Photo>
}