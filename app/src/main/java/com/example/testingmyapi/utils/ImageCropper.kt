package com.example.testingmyapi.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.UUID

object ImageCropper {

    private const val CROP_REQUEST_CODE = 69

    fun startCrop(activity: Activity, sourceUri: Uri) {
        val destinationUri = Uri.fromFile(
            File(activity.cacheDir, "cropped_" + UUID.randomUUID().toString() + ".jpg")
        )

        val uCrop = UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withMaxResultSize(512, 512)

        uCrop.start(activity, CROP_REQUEST_CODE)
    }

    fun handleCropResult(data: Intent?): Bitmap? {
        return try {
            val resultUri = UCrop.getOutput(data!!)
            resultUri?.let {
                val inputStream = (data as? Activity)?.contentResolver?.openInputStream(it)
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            null
        }
    }
}