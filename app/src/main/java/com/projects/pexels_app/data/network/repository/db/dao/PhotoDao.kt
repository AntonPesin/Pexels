package com.projects.pexels_app.data.network.repository.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.projects.pexels_app.data.network.models.MediaModel

@Dao
interface PhotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(mediaModel: MediaModel)

    @Query("DELETE FROM photos WHERE id = :photoId")
    fun delete(photoId:Int?)

    @Query("SELECT * FROM photos WHERE id =:photoId")
    fun get(photoId: Int?): MediaModel

    @Query("SELECT COUNT(*) FROM photos")
    suspend fun getPhotoCount(): Int

    @Query("SELECT * FROM photos")
    fun getPagingPhotos(): PagingSource<Int, MediaModel>

    @Query("SELECT COUNT(*) FROM photos WHERE id = :id")
    fun exists(id: Int?): Boolean

}