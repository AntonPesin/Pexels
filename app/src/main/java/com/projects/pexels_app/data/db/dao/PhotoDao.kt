package com.projects.pexels_app.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.projects.pexels_app.domain.models.Photo

@Dao
interface PhotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(photo: Photo)

    @Query("DELETE FROM photos WHERE id = :photoId")
    fun delete(photoId:Int?)

    @Query("SELECT * FROM photos WHERE id =:photoId")
    fun get(photoId: Int?): Photo

    @Query("SELECT COUNT(*) FROM photos")
    suspend fun getPhotoCount(): Int

    @Query("SELECT * FROM photos")
    fun getPagingPhotos(): PagingSource<Int, Photo>

    @Query("SELECT COUNT(*) FROM photos WHERE id = :id")
    fun exists(id: Int?): Boolean

}