package com.projects.pexels_app

import android.app.Application
import androidx.room.Room
import com.projects.pexels_app.data.network.repository.db.PhotoDataBase
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application(){
    lateinit var photoDataBase: PhotoDataBase

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        photoDataBase = Room
            .databaseBuilder(
                this,
                PhotoDataBase::class.java,
                "photos_db"
            )
            .fallbackToDestructiveMigration()
            .build()
    }

    companion object {
        lateinit var INSTANCE :App
    }

}

