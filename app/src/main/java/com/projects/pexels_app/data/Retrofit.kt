package com.projects.pexels_app.data

import com.projects.pexels_app.data.network.lists.CollectionPhotoList
import com.projects.pexels_app.data.network.lists.CollectionsList
import com.projects.pexels_app.data.network.lists.PhotoList
import com.projects.pexels_app.data.network.models.MediaModel
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient

import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

const val BASE_URL = "https://api.pexels.com/v1/"
const val API_KEY = "ArNOuHM7EBlltiQPXLY0WVGDiCYPyjjlHc70Pp0xzOXy5JjFLoDoo2r8"

object Retrofit {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()


    private val retrofitPexels = Retrofit
        .Builder()
        .client(
            OkHttpClient.Builder().addInterceptor(HttpLoggingInterceptor()).build()
        )
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val search: PexelsApiService = retrofitPexels.create(PexelsApiService::class.java)

    interface PexelsApiService {

        //Get Featured Collections
        @Headers("Authorization:$API_KEY")
        @GET("collections/featured?per_page=7")
        suspend fun getCollections(
        ): CollectionsList

        //Get Curated photos
        @Headers("Authorization:$API_KEY")
        @GET("curated?type=photos&per_page=30&page=1")
        suspend fun getCuratedPhotos(
        ): PhotoList

        //Search photos
        @Headers("Authorization:$API_KEY")
        @GET("search")
        suspend fun searchPhotos(
            @Query("query") keyword: String,
            @Query("page") page: Int,
            @Query("per_page") perPage: Int
        ): PhotoList

        //Get photo
        @Headers("Authorization:$API_KEY")
        @GET("photos/{id}")
        suspend fun getPhoto(
            @Path("id") id: Int,
        ): MediaModel
    }
}