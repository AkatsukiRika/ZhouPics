package com.tgwgroup.facecomparelib

import android.graphics.Bitmap
import com.tgwgroup.baselib.utils.LogUtil
import com.tgwgroup.baselib.utils.bitmapToByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object FacePPUtils {
    private const val TAG = "FacePlusPlusUtils"
    private const val FACE_API_ENDPOINT = "https://api-cn.faceplusplus.com/facepp/"
    private const val FACE_API_KEY = BuildConfig.FACE_API_KEY
    private const val FACE_API_SECRET = BuildConfig.FACE_API_SECRET
    private const val FACE_API_COMPARE_PATH = "v3/compare"
    private val scope = MainScope()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun compareFaces(
        bitmap1: Bitmap,
        bitmap2: Bitmap,
        callback: CompareFacesCallback? = null
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                callback?.onPrepare()

                val imageBytes1 = bitmapToByteArray(bitmap1)
                val imageBytes2 = bitmapToByteArray(bitmap2)

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("api_key", FACE_API_KEY)
                    .addFormDataPart("api_secret", FACE_API_SECRET)
                    .addFormDataPart(
                        "image_file1",
                        "face1.jpg",
                        imageBytes1.toRequestBody("image/jpeg".toMediaType())
                    )
                    .addFormDataPart(
                        "image_file2",
                        "face2.jpg",
                        imageBytes2.toRequestBody("image/jpeg".toMediaType())
                    )
                    .build()

                val request = Request.Builder()
                    .url(FACE_API_ENDPOINT + FACE_API_COMPARE_PATH)
                    .post(requestBody)
                    .build()
                callback?.onRequest()

                okHttpClient.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: java.io.IOException) {
                        LogUtil.e(TAG, "Error verifying faces: ${e.message}")
                        callback?.onError(e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        callback?.onResponse()
                        val result = response.body?.string()
                        if (response.isSuccessful) {
                            result?.let {
                                val jsonObject = JSONObject(it)
                                if (jsonObject.has("confidence")) {
                                    val confidence = jsonObject.optDouble("confidence", 0.0).toFloat()
                                    callback?.onSuccess(confidence)
                                } else {
                                    callback?.onSuccess(null)
                                }
                            } ?: run {
                                callback?.onSuccess(null)
                            }
                        } else {
                            callback?.onError(IllegalStateException("Error verifying faces: ${response.code} ${response.message}"))
                        }
                    }
                })
            } catch (e: Exception) {
                LogUtil.e(TAG, "Error verifying faces: ${e.message}")
                callback?.onError(e)
            }
        }
    }
}