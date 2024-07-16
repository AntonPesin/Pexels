package com.projects.pexels_app.di

import android.content.Context
import androidx.room.Room
import com.projects.pexels_app.data.api.ApiService
import com.projects.pexels_app.data.repositories.MainRepository
import com.projects.pexels_app.data.repositories.RepositoryImpl
import com.projects.pexels_app.data.db.PhotoDataBase
import com.projects.pexels_app.data.repositories.ConnectivityRepository
import com.projects.pexels_app.usecases.details.DetailsUseCase
import com.projects.pexels_app.usecases.details.IDetailsUseCase
import com.projects.pexels_app.usecases.home.curated.CuratedPhotosUseCase
import com.projects.pexels_app.usecases.home.curated.IGetCuratedPhotosUseCase
import com.projects.pexels_app.usecases.home.collections.ILoadCollectionsUseCase
import com.projects.pexels_app.usecases.home.search.ISearchUseCase
import com.projects.pexels_app.usecases.home.collections.LoadCollectionsUseCase
import com.projects.pexels_app.usecases.home.search.SearchUseCase
import com.projects.pexels_app.utils.Constant
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun providesDatabase(@ApplicationContext context: Context): PhotoDataBase {
        return Room.databaseBuilder(
            context,
            PhotoDataBase::class.java,
            Constant.DB_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun providesLoggingInterceptor() = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    @Provides
    @Singleton
    fun provideMainRepository(
        apiService: ApiService,
    ): MainRepository {
        return RepositoryImpl(apiService)
    }

    @Provides
    @Singleton
    fun provideConnectivityRepository(
        @ApplicationContext context: Context
    ): ConnectivityRepository {
        return ConnectivityRepository(context)
    }

    @Provides
    @Singleton
    fun provideDetailsUseCase(
        repository: MainRepository,
        photoDataBase: PhotoDataBase
    ): IDetailsUseCase {
        return DetailsUseCase(repository, photoDataBase)
    }


    @Provides
    @Singleton
    fun providesOkHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .readTimeout(15, TimeUnit.SECONDS)
            .connectTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl(Constant.BASE_URL)
        .addConverterFactory(
            MoshiConverterFactory.create(
                Moshi.Builder()
                    .add(KotlinJsonAdapterFactory())
                    .build()
            )
        )
        .build()

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService = retrofit.create(ApiService::class.java)

    @Provides
    @Singleton
    fun provideLoadCollectionsUseCase(repository: MainRepository): ILoadCollectionsUseCase {
        return LoadCollectionsUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetCuratedPhotosUseCase(repository: MainRepository): IGetCuratedPhotosUseCase {
        return CuratedPhotosUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideSearchUseCase(repository: MainRepository): ISearchUseCase {
        return SearchUseCase(repository)
    }

}