package com.tgwgroup.zhoupics.utils

import android.content.ContentUris
import android.content.Context
import android.graphics.BitmapFactory
import android.provider.MediaStore
import com.tgwgroup.zhoupics.R
import com.tgwgroup.zhoupics.ui.gallery.AlbumItem
import com.tgwgroup.zhoupics.ui.gallery.ImageFormat
import com.tgwgroup.zhoupics.ui.gallery.ImageItem

fun getAlbumList(context: Context, onSelectItem: ((bucketId: String?) -> Unit)? = null): List<AlbumItem> {
    val tempAlbumList = mutableListOf<AlbumItem>()

    val projection = arrayOf(
        MediaStore.Images.Media.BUCKET_ID,
        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
        MediaStore.Images.Media.DATA
    )
    val sortOrder = "${MediaStore.Images.Media.DATE_MODIFIED} DESC"
    val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    context.contentResolver.query(uri, projection, null, null, sortOrder)?.use { cursor ->
        val bucketIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID)
        val bucketNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)

        while (cursor.moveToNext()) {
            val bucketId = cursor.getString(bucketIdColumn)
            val bucketName = cursor.getString(bucketNameColumn)

            if (bucketName != null && tempAlbumList.find { it.name == bucketName } == null) {
                tempAlbumList.add(AlbumItem(
                    id = bucketId,
                    name = bucketName,
                    images = getImagesFromAlbum(context, bucketId),
                    onClick = {
                        onSelectItem?.invoke(bucketId)
                    }
                ))
            }
        }
    }

    tempAlbumList.add(0, AlbumItem(
        id = null,
        name = context.getString(R.string.all_photos),
        images = getImagesFromAlbum(context, null),
        onClick = {
            onSelectItem?.invoke(null)
        }
    ))

    if (tempAlbumList.isNotEmpty()) {
        tempAlbumList[0].selected = true
    }

    return tempAlbumList
}

fun getImagesFromAlbum(context: Context, bucketId: String?): List<ImageItem> {
    val images = mutableListOf<ImageItem>()

    val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.BUCKET_ID,
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
        MediaStore.Images.Media.DATE_ADDED,
        MediaStore.Images.Media.SIZE
    )
    
    val selection: String?
    val selectionArgs: Array<String>?
    
    if (bucketId != null) {
        selection = "${MediaStore.Images.Media.BUCKET_ID} = ?"
        selectionArgs = arrayOf(bucketId)
    } else {
        selection = null
        selectionArgs = null
    }
    
    val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
    context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection, selection, selectionArgs, sortOrder
    )?.use { cursor ->
        val idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID)
        val nameColumn = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
        val dateAddedColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED)
        val sizeColumn = cursor.getColumnIndex(MediaStore.Images.Media.SIZE)

        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumn)
            val name = cursor.getString(nameColumn)
            val dateAdded = cursor.getLong(dateAddedColumn)
            val size = cursor.getLong(sizeColumn)
            val contentUri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id
            )

            var width = 0
            var height = 0
            var format = ImageFormat.UNKNOWN
            runCatching {
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                context.contentResolver.openInputStream(contentUri)?.use { input ->
                    BitmapFactory.decodeStream(input, null, options)
                }
                width = options.outWidth
                height = options.outHeight
                format = when (options.outMimeType) {
                    "image/jpeg" -> ImageFormat.JPEG
                    "image/png" -> ImageFormat.PNG
                    "image/gif" -> ImageFormat.GIF
                    "image/webp" -> ImageFormat.WEBP
                    "image/heic", "image/heif" -> ImageFormat.HEIC
                    "image/bmp" -> ImageFormat.BMP
                    else -> ImageFormat.UNKNOWN
                }
            }.onFailure {
                it.printStackTrace()
            }

            images.add(ImageItem(name, contentUri, dateAdded, size, width, height, format))
        }
    }

    return images
}