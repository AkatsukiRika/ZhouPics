package com.tgwgroup.facecomparelib

import android.content.Context
import android.graphics.Bitmap
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.rekognition.AmazonRekognitionClient
import com.amazonaws.services.rekognition.model.CompareFacesRequest
import com.amazonaws.services.rekognition.model.Image
import com.tgwgroup.baselib.utils.LogUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

object AwsUtils {
    private const val TAG = "AwsUtils"
    private const val IDENTITY_POOL_ID = BuildConfig.IDENTITY_POOL_ID

    private var credentialsProvider: CognitoCachingCredentialsProvider? = null
    private var rekognitionClient: AmazonRekognitionClient? = null

    private val scope = MainScope()

    fun initAwsSdk(context: Context) {
        try {
            credentialsProvider = CognitoCachingCredentialsProvider(
                context,
                IDENTITY_POOL_ID,
                Regions.AP_SOUTHEAST_2
            )
            rekognitionClient = AmazonRekognitionClient(credentialsProvider)
            LogUtil.d(TAG, "AWS SDK initialized successfully")
        } catch (e: Exception) {
            LogUtil.e(TAG, "Failed to initialize AWS SDK: ${e.message}")
        }
    }

    fun compareFaces(
        bitmap1: Bitmap,
        bitmap2: Bitmap,
        similarityThreshold: Float = 70f,
        callback: CompareFacesCallback? = null
    ) {
        if (rekognitionClient == null) {
            LogUtil.e(TAG, "Rekognition client is not initialized")
            callback?.onError(IllegalStateException("Rekognition client is not initialized"))
            return
        }

       scope.launch(Dispatchers.IO) {
            try {
                callback?.onPrepare()

                val stream1 = ByteArrayOutputStream()
                bitmap1.compress(Bitmap.CompressFormat.JPEG, 70, stream1)
                val imageBytes1: ByteBuffer = ByteBuffer.wrap(stream1.toByteArray())

                val stream2 = ByteArrayOutputStream()
                bitmap2.compress(Bitmap.CompressFormat.JPEG, 70, stream2)
                val imageBytes2: ByteBuffer = ByteBuffer.wrap(stream2.toByteArray())

                val sourceImage = Image().withBytes(imageBytes1)
                val targetImage = Image().withBytes(imageBytes2)

                val request = CompareFacesRequest()
                    .withSourceImage(sourceImage)
                    .withTargetImage(targetImage)
                    .withSimilarityThreshold(similarityThreshold)
                LogUtil.d(TAG, "Sending CompareFaces request...")
                callback?.onRequest()

                val result = rekognitionClient?.compareFaces(request)
                LogUtil.d(TAG, "Received CompareFaces response.")
                callback?.onResponse()

                val faceMatches = result?.faceMatches
                withContext(Dispatchers.Main) {
                    if (!faceMatches.isNullOrEmpty()) {
                        val match = faceMatches[0]
                        val similarity = match.similarity
                        LogUtil.i(TAG, "Faces matched with similarity: $similarity")
                        delay(1000)
                        callback?.onSuccess(similarity)
                    } else {
                        LogUtil.i(TAG, "No faces matched.")
                        delay(1000)
                        callback?.onSuccess(null)
                    }
                }
            } catch (e: Exception) {
                LogUtil.e(TAG, "Error comparing faces: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    callback?.onError(e)
                }
            }
        }
    }
}