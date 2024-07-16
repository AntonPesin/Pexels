package com.projects.pexels_app.domain



sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(val code: Int, val errorMsg: String?) : NetworkResult<Nothing>()
}