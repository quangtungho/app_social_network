package vn.techres.line.helper

import android.media.ExifInterface
import android.util.Log
import java.io.IOException

object ImageRotationDetectionHelper {
    fun getCameraPhotoOrientation(imageFilePath: String?): Int {
        var rotate = 0
        try {
            val exif = ExifInterface(imageFilePath!!)
            val exifOrientation = exif
                .getAttribute(ExifInterface.TAG_ORIENTATION)
            Log.d("exifOrientation", exifOrientation!!)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            Log.d(
                ImageRotationDetectionHelper::class.java.simpleName,
                "orientation :$orientation"
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
                ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
                ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return rotate
    }
}