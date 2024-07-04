package com.projects.pexels_app.utils

interface Downloader {
    fun downloadImage(url:String,title:String) : Long
}