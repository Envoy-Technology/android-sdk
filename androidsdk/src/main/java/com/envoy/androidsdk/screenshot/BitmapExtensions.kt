package com.envoy.androidsdk.screenshot

import android.graphics.Bitmap
import android.util.Base64
import java.io.ByteArrayOutputStream

/**
 * Converts a Bitmap to a Base64 encoded string.
 *
 * @param format The compression format (default: PNG)
 * @param quality The compression quality (0-100, default: 100)
 * @return Base64 encoded string of the bitmap
 */
fun Bitmap.toBase64(
    format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
    quality: Int = 100
): String {
    val byteArrayOutputStream = ByteArrayOutputStream()
    this.compress(format, quality, byteArrayOutputStream)
    val byteArray = byteArrayOutputStream.toByteArray()
    return Base64.encodeToString(byteArray, Base64.NO_WRAP)
}
