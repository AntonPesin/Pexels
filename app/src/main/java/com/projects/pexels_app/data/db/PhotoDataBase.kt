package com.projects.pexels_app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.projects.pexels_app.domain.models.Photo
import com.projects.pexels_app.data.db.dao.PhotoDao

@Database(entities = [Photo::class], version = 1, exportSchema = false)
abstract class PhotoDataBase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
}
