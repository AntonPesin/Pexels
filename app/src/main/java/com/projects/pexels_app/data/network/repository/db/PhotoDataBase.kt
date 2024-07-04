package com.projects.pexels_app.data.network.repository.db

import android.provider.MediaStore.Audio.Media
import androidx.databinding.adapters.Converters
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.projects.pexels_app.data.network.models.MediaModel
import com.projects.pexels_app.data.network.repository.db.dao.PhotoDao

@Database(entities = [MediaModel::class], version = 1)
abstract class PhotoDataBase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
}
