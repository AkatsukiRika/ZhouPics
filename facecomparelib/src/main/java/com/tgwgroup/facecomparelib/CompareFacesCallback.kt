package com.tgwgroup.facecomparelib

interface CompareFacesCallback {
    fun onPrepare()
    fun onRequest()
    fun onResponse()
    fun onSuccess(similarity: Float?)
    fun onError(exception: Exception)
}