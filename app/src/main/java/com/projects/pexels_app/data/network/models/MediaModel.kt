package com.projects.pexels_app.data.network.models

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "photos")
data class MediaModel(
    @PrimaryKey(autoGenerate = true) val id:Int,
    @ColumnInfo(name = "name") val photographer:String,
    @Embedded val src:Src,
)

data class Src(
    val medium:String
)


