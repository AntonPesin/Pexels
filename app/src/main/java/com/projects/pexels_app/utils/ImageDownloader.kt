package com.projects.pexels_app.utils

import android.app.DownloadManager
import android.content.Context
import android.os.Environment
import androidx.core.net.toUri

class ImageDownloader (context: Context): Downloader {

    private val downloadManager = context.getSystemService(DownloadManager::class.java)

    override fun downloadImage(url: String,title:String): Long {
        val request = DownloadManager.Request(url.toUri())
            .setMimeType("image/jpeg")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setTitle(title)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"image.jpg")

        return downloadManager.enqueue(request)
    }


}