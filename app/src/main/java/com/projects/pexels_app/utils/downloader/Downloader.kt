package com.projects.pexels_app.utils.downloader

interface Downloader {
    fun downloadImage(url:String,title:String) : Long
}