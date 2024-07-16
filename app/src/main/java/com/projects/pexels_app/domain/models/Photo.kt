package com.projects.pexels_app.domain.models

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "photos")
data class Photo(
    @PrimaryKey(autoGenerate = true) val id:Int,
    @ColumnInfo(name = "name") val photographer:String,
    @Embedded val src: Src,
)



